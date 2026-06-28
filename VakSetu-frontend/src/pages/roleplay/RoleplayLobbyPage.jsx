import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import LoadingBlock from '../../components/common/LoadingBlock'
import { websocketEvents } from '../../constants/websocket'
import { useWebSocketEvents } from '../../hooks/useWebSocketEvents'
import MatchmakingService from '../../services/MatchmakingService'

function RoleplayLobbyPage() {
  const { events } = useWebSocketEvents()
  const [status, setStatus] = useState(null)
  const [error, setError] = useState('')
  const [loadingStatus, setLoadingStatus] = useState(true)
  const matchedSessionId =
    events[0]?.eventType === websocketEvents.matchFound && events[0]?.payload?.sessionType === 'ROLEPLAY'
      ? events[0].payload.sessionId
      : null

  async function refreshStatus() {
    setStatus(await MatchmakingService.getRoleplayStatus())
  }

  useEffect(() => {
    MatchmakingService.getRoleplayStatus()
      .then(setStatus)
      .catch((exception) => setError(exception.message))
      .finally(() => setLoadingStatus(false))
  }, [])

  useEffect(() => {
    const latestEvent = events[0]

    if (latestEvent?.eventType === websocketEvents.matchFound && latestEvent.payload?.sessionType === 'ROLEPLAY') {
      MatchmakingService.getRoleplayStatus()
        .then(setStatus)
        .catch(() => {})
    }
  }, [events])

  async function joinQueue() {
    setError('')

    try {
      await MatchmakingService.joinRoleplayQueue()
      await refreshStatus()
    } catch (exception) {
      setError(exception.message)
    }
  }

  async function leaveQueue() {
    await MatchmakingService.leaveRoleplayQueue()
    await refreshStatus()
  }

  return (
    <section className="page-stack">
      <header>
        <p className="eyebrow">Roleplay</p>
        <h1>Roleplay queue</h1>
      </header>
      <div className="toolbar">
        <button type="button" onClick={joinQueue}>
          Join queue
        </button>
        <button type="button" className="ghost-button" onClick={leaveQueue}>
          Leave
        </button>
      </div>
      {loadingStatus && <LoadingBlock label="Loading queue status" />}
      {matchedSessionId && (
        <Link to={`/roleplay/session/${matchedSessionId}`} className="inline-link">
          Open matched roleplay
        </Link>
      )}
      {status && <p className="muted">Queue size: {status.queueSize}</p>}
      {error && <p className="error-text">{error}</p>}
    </section>
  )
}

export default RoleplayLobbyPage
