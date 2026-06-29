import { useEffect, useMemo, useState } from 'react'
import Alert from '../common/Alert'
import { websocketTopics, webRtcSignalTypes } from '../../constants/websocket'
import { useAuth } from '../../hooks/useAuth'
import { useWebSocketEvents } from '../../hooks/useWebSocketEvents'

const INITIAL_SIGNAL = '{\n  "type": "offer",\n  "sdp": ""\n}'

function WebRtcSignalingPanel({ sessionId, sessionType, partner }) {
  const { currentUser } = useAuth()
  const webSocket = useWebSocketEvents()
  const connected = webSocket?.connected ?? false
  const subscribe = webSocket?.subscribe
  const publish = webSocket?.publish
  const [signalType, setSignalType] = useState(webRtcSignalTypes.offer)
  const [signalData, setSignalData] = useState(INITIAL_SIGNAL)
  const [messages, setMessages] = useState([])
  const [error, setError] = useState('')

  const topic = useMemo(() => websocketTopics.webRtcSession(sessionId), [sessionId])

  useEffect(() => {
    if (!connected || !sessionId || !subscribe) {
      return undefined
    }

    return subscribe(topic, (event) => {
      const payload = event.payload

      if (!payload || payload.senderUserId === currentUser?.id) {
        return
      }

      if (payload.receiverUserId && payload.receiverUserId !== currentUser?.id) {
        return
      }

      setMessages((currentMessages) => [event, ...currentMessages].slice(0, 10))
    })
  }, [connected, currentUser?.id, sessionId, subscribe, topic])

  function handleSignalTypeChange(nextSignalType) {
    setSignalType(nextSignalType)

    if (nextSignalType === webRtcSignalTypes.iceCandidate) {
      setSignalData('{\n  "candidate": "",\n  "sdpMid": "",\n  "sdpMLineIndex": 0\n}')
      return
    }

    setSignalData(`{\n  "type": "${nextSignalType.toLowerCase()}",\n  "sdp": ""\n}`)
  }

  function sendSignal() {
    setError('')

    try {
      publish('/app/webrtc/signal', {
        sessionId: Number(sessionId),
        sessionType,
        receiverUserId: partner?.id,
        signalType,
        signalData: JSON.parse(signalData),
      })
    } catch (exception) {
      setError(exception.message)
    }
  }

  return (
    <section className="form-card">
      <header>
        <p className="eyebrow">WebRTC Signaling</p>
        <h2>{connected ? 'Connection ready' : 'Waiting for realtime connection'}</h2>
      </header>
      <div className="toolbar">
        <label>
          Signal
          <select value={signalType} onChange={(event) => handleSignalTypeChange(event.target.value)}>
            <option value={webRtcSignalTypes.offer}>Offer</option>
            <option value={webRtcSignalTypes.answer}>Answer</option>
            <option value={webRtcSignalTypes.iceCandidate}>ICE candidate</option>
          </select>
        </label>
      </div>
      <label>
        Payload JSON
        <textarea value={signalData} onChange={(event) => setSignalData(event.target.value)} rows={7} />
      </label>
      <button type="button" onClick={sendSignal} disabled={!connected}>
        Send signal
      </button>
      <Alert variant="error">{error}</Alert>
      <div className="history-list">
        {messages.length === 0 ? (
          <p className="muted">Incoming offer, answer, and ICE messages will appear here.</p>
        ) : (
          messages.map((event) => (
            <article key={`${event.eventType}-${event.timestamp}`}>
              <div>
                <strong>{event.payload.signalType}</strong>
                <span>From user #{event.payload.senderUserId}</span>
              </div>
              <code>{JSON.stringify(event.payload.signalData)}</code>
            </article>
          ))
        )}
      </div>
    </section>
  )
}

export default WebRtcSignalingPanel
