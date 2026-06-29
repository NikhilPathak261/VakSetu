import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import EventPanel from './EventPanel'

const websocketMocks = vi.hoisted(() => ({
  clearEvents: vi.fn(),
  connected: false,
  events: [],
}))

vi.mock('../../hooks/useWebSocketEvents', () => ({
  useWebSocketEvents: () => ({
    clearEvents: websocketMocks.clearEvents,
    connected: websocketMocks.connected,
    events: websocketMocks.events,
  }),
}))

describe('EventPanel', () => {
  beforeEach(() => {
    websocketMocks.clearEvents.mockReset()
    websocketMocks.connected = false
    websocketMocks.events = []
  })

  it('renders disconnected empty realtime state', () => {
    render(<EventPanel />)

    expect(screen.getByText('Disconnected')).toBeInTheDocument()
    expect(screen.getByText('No events yet')).toBeInTheDocument()
  })

  it('renders realtime events and clears them through the websocket context', async () => {
    const user = userEvent.setup()
    websocketMocks.connected = true
    websocketMocks.events = [
      { eventType: 'MATCH_FOUND', message: 'Debate matched', timestamp: '2026-06-29T10:00:00' },
      { eventType: 'STAR_RECEIVED', message: 'Priya received a star', timestamp: '2026-06-29T10:01:00' },
    ]

    render(<EventPanel />)

    expect(screen.getByText('Connected')).toBeInTheDocument()
    expect(screen.getByText('MATCH_FOUND')).toBeInTheDocument()
    expect(screen.getByText('Priya received a star')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /clear/i }))

    expect(websocketMocks.clearEvents).toHaveBeenCalledTimes(1)
  })
})
