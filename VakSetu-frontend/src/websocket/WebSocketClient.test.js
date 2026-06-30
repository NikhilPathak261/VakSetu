import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { createWebSocketClient } from './WebSocketClient'

vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn(function Client(config) {
    this.config = config
  }),
}))

vi.mock('sockjs-client', () => ({
  default: vi.fn(function SockJS(url) {
    this.url = url
  }),
}))

describe('createWebSocketClient', () => {
  beforeEach(() => {
    Client.mockClear()
    SockJS.mockClear()
  })

  it('creates a STOMP client with reconnect, heartbeat, and lifecycle handlers', () => {
    const onConnect = vi.fn()
    const onDisconnect = vi.fn()

    const client = createWebSocketClient('access token', onConnect, onDisconnect)

    expect(Client).toHaveBeenCalledWith({
      webSocketFactory: expect.any(Function),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect,
      onDisconnect,
      onStompError: onDisconnect,
      onWebSocketClose: onDisconnect,
    })
    expect(client.config.onConnect).toBe(onConnect)
  })

  it('passes the encoded access token to the SockJS handshake URL', () => {
    const client = createWebSocketClient('token with spaces', vi.fn(), vi.fn())

    const socket = client.config.webSocketFactory()

    expect(SockJS).toHaveBeenCalledWith('http://localhost:8080/ws?token=token%20with%20spaces')
    expect(socket.url).toBe('http://localhost:8080/ws?token=token%20with%20spaces')
  })
})
