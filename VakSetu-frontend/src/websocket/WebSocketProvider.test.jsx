import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useWebSocketEvents } from '../hooks/useWebSocketEvents'
import { WebSocketProvider } from './WebSocketProvider'
import { createWebSocketClient } from './WebSocketClient'

const authMocks = vi.hoisted(() => ({
  accessToken: 'access-token',
  isAuthenticated: true,
}))

const clientMocks = vi.hoisted(() => ({
  client: null,
  handlersByTopic: {},
  unsubscribe: vi.fn(),
}))

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    accessToken: authMocks.accessToken,
    isAuthenticated: authMocks.isAuthenticated,
  }),
}))

vi.mock('./WebSocketClient', () => ({
  createWebSocketClient: vi.fn((accessToken, onConnect, onDisconnect) => {
    const client = {
      activate: vi.fn(() => onConnect()),
      connected: true,
      deactivate: vi.fn(() => onDisconnect()),
      publish: vi.fn(),
      subscribe: vi.fn((topic, handler) => {
        clientMocks.handlersByTopic[topic] = handler
        return { unsubscribe: clientMocks.unsubscribe }
      }),
    }
    clientMocks.client = client
    return client
  }),
}))

function WebSocketProbe() {
  const { clearEvents, connected, events, publish, subscribe } = useWebSocketEvents()

  function handleSubscribe() {
    subscribe('/topic/custom', (event) => {
      window.dispatchEvent(new CustomEvent('probe-event', { detail: event.message }))
    })
  }

  function handlePublish() {
    publish('/app/webrtc/signal', { sessionId: 5, signalType: 'OFFER' })
  }

  return (
    <section>
      <p>{connected ? 'connected' : 'disconnected'}</p>
      <p>Events: {events.length}</p>
      <ul>
        {events.map((event) => (
          <li key={`${event.eventType}-${event.timestamp || event.message}`}>{event.message}</li>
        ))}
      </ul>
      <button type="button" onClick={clearEvents}>Clear events</button>
      <button type="button" onClick={handleSubscribe}>Subscribe custom</button>
      <button type="button" onClick={handlePublish}>Publish signal</button>
    </section>
  )
}

describe('WebSocketProvider', () => {
  beforeEach(() => {
    authMocks.accessToken = 'access-token'
    authMocks.isAuthenticated = true
    clientMocks.client = null
    clientMocks.handlersByTopic = {}
    clientMocks.unsubscribe.mockReset()
    createWebSocketClient.mockClear()
  })

  it('does not create a realtime client until the user is authenticated', () => {
    authMocks.accessToken = ''
    authMocks.isAuthenticated = false

    render(
      <WebSocketProvider>
        <WebSocketProbe />
      </WebSocketProvider>,
    )

    expect(screen.getByText('disconnected')).toBeInTheDocument()
    expect(createWebSocketClient).not.toHaveBeenCalled()
  })

  it('connects with the auth token and subscribes to shared event topics', async () => {
    render(
      <WebSocketProvider>
        <WebSocketProbe />
      </WebSocketProvider>,
    )

    expect(await screen.findByText('connected')).toBeInTheDocument()
    expect(createWebSocketClient).toHaveBeenCalledWith('access-token', expect.any(Function), expect.any(Function))
    expect(clientMocks.client.activate).toHaveBeenCalledTimes(1)
    expect(clientMocks.client.subscribe).toHaveBeenCalledWith('/topic/match', expect.any(Function))
    expect(clientMocks.client.subscribe).toHaveBeenCalledWith('/topic/gd', expect.any(Function))
    expect(clientMocks.client.subscribe).toHaveBeenCalledWith('/topic/system', expect.any(Function))
  })

  it('stores realtime events, falls back for malformed payloads, and clears events', async () => {
    const user = userEvent.setup()

    render(
      <WebSocketProvider>
        <WebSocketProbe />
      </WebSocketProvider>,
    )

    await screen.findByText('connected')
    clientMocks.handlersByTopic['/topic/match']({
      body: JSON.stringify({
        eventType: 'MATCH_FOUND',
        message: 'Debate matched',
        timestamp: '2026-07-01T10:00:00',
      }),
    })
    clientMocks.handlersByTopic['/topic/system']({ body: 'not json' })

    expect(await screen.findByText('Debate matched')).toBeInTheDocument()
    expect(screen.getByText('not json')).toBeInTheDocument()
    expect(screen.getByText('Events: 2')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /clear events/i }))

    expect(screen.getByText('Events: 0')).toBeInTheDocument()
  })

  it('publishes JSON payloads through the active STOMP client', async () => {
    const user = userEvent.setup()

    render(
      <WebSocketProvider>
        <WebSocketProbe />
      </WebSocketProvider>,
    )

    await screen.findByText('connected')
    await user.click(screen.getByRole('button', { name: /publish signal/i }))

    expect(clientMocks.client.publish).toHaveBeenCalledWith({
      destination: '/app/webrtc/signal',
      body: JSON.stringify({ sessionId: 5, signalType: 'OFFER' }),
    })
  })

  it('supports custom subscriptions and unsubscribes on cleanup', async () => {
    const user = userEvent.setup()
    const receivedMessages = []
    window.addEventListener('probe-event', (event) => receivedMessages.push(event.detail))

    const { unmount } = render(
      <WebSocketProvider>
        <WebSocketProbe />
      </WebSocketProvider>,
    )

    await screen.findByText('connected')
    await user.click(screen.getByRole('button', { name: /subscribe custom/i }))
    clientMocks.handlersByTopic['/topic/custom']({ body: JSON.stringify({ message: 'Custom received' }) })

    expect(receivedMessages).toEqual(['Custom received'])

    unmount()

    await waitFor(() => {
      expect(clientMocks.client.deactivate).toHaveBeenCalledTimes(1)
    })
  })
})
