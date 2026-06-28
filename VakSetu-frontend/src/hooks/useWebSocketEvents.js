import { useContext } from 'react'
import WebSocketContext from '../websocket/WebSocketContextValue'

export function useWebSocketEvents() {
  return useContext(WebSocketContext)
}
