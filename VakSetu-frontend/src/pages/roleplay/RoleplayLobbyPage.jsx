import { useEffect, useState } from 'react'
import { websocketEvents } from '../../constants/websocket'
import { useWebSocketEvents } from '../../hooks/useWebSocketEvents'
import MatchmakingService from '../../services/MatchmakingService'

function RoleplayLobbyPage() {
  const { events } = useWebSocketEvents()
  const [status, setStatus] = useState(null)
  const [error, setError] = useState('')

  async function refreshStatus() {
    setStatus(await MatchmakingService.getRoleplayStatus())
  }

  useEffect(() => {
    MatchmakingService.getRoleplayStatus()
      .then(setStatus)
      .catch((exception) => setError(exception.message))
  }, [])

  useEffect(() => {
    if (events[0]?.eventType === websocketEvents.matchFound) {
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
      {status && <p className="muted">Queue size: {status.queueSize}</p>}
      {error && <p className="error-text">{error}</p>}
    </section>
  )
}

export default RoleplayLobbyPage
