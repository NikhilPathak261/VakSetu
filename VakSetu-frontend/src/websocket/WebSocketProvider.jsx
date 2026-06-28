import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { websocketTopics } from '../constants/websocket'
import { useAuth } from '../hooks/useAuth'
import { createWebSocketClient } from './WebSocketClient'
import WebSocketContext from './WebSocketContextValue'

const MAX_EVENTS = 20

export function WebSocketProvider({ children }) {
  const { accessToken, isAuthenticated } = useAuth()
  const clientRef = useRef(null)
  const [connected, setConnected] = useState(false)
  const [events, setEvents] = useState([])

  const addEvent = useCallback((event) => {
    setEvents((currentEvents) => [event, ...currentEvents].slice(0, MAX_EVENTS))
  }, [])

  const clearEvents = useCallback(() => {
    setEvents([])
  }, [])

  const subscribe = useCallback((topic, handler) => {
    const client = clientRef.current

    if (!client?.connected) {
      return () => {}
    }

    const subscription = client.subscribe(topic, (message) => {
      handler(JSON.parse(message.body))
    })

    return () => subscription.unsubscribe()
  }, [])

  const publish = useCallback((destination, payload) => {
    const client = clientRef.current

    if (!client?.connected) {
      throw new Error('Realtime connection is not ready')
    }

    client.publish({
      destination,
      body: JSON.stringify(payload),
    })
  }, [])

  useEffect(() => {
    if (!isAuthenticated || !accessToken) {
      return undefined
    }

    const client = createWebSocketClient(
      accessToken,
      () => {
        setConnected(true)

        Object.values(websocketTopics).filter((topic) => typeof topic === 'string').forEach((topic) => {
          client.subscribe(topic, (message) => {
            try {
              addEvent(JSON.parse(message.body))
            } catch {
              addEvent({
                eventType: 'UNKNOWN',
                message: message.body,
                timestamp: new Date().toISOString(),
              })
            }
          })
        })
      },
      () => setConnected(false),
    )

    clientRef.current = client
    client.activate()

    return () => {
      client.deactivate()
      clientRef.current = null
      setConnected(false)
    }
  }, [accessToken, addEvent, isAuthenticated])

  const value = useMemo(
    () => ({
      connected,
      events,
      clearEvents,
      subscribe,
      publish,
    }),
    [connected, events, clearEvents, subscribe, publish],
  )

  return <WebSocketContext.Provider value={value}>{children}</WebSocketContext.Provider>
}
