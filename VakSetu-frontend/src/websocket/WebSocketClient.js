import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || API_BASE_URL

export function createWebSocketClient(accessToken, onConnect, onDisconnect) {
  return new Client({
    webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws?token=${encodeURIComponent(accessToken)}`),
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect,
    onDisconnect,
    onStompError: onDisconnect,
    onWebSocketClose: onDisconnect,
  })
}
