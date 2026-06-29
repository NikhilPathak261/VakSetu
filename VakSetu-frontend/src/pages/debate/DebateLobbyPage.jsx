import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import { websocketEvents } from '../../constants/websocket'
import { useWebSocketEvents } from '../../hooks/useWebSocketEvents'
import MatchmakingService from '../../services/MatchmakingService'
import TopicService from '../../services/TopicService'

function DebateLobbyPage() {
  const { events } = useWebSocketEvents()
  const [topics, setTopics] = useState([])
  const [topicId, setTopicId] = useState('')
  const [status, setStatus] = useState(null)
  const [error, setError] = useState('')
  const [loadingTopics, setLoadingTopics] = useState(true)
  const matchedSessionId =
    events[0]?.eventType === websocketEvents.matchFound && events[0]?.payload?.sessionType === 'DEBATE'
      ? events[0].payload.sessionId
      : null

  useEffect(() => {
    TopicService.getTopics()
      .then(setTopics)
      .catch((exception) => setError(exception.message))
      .finally(() => setLoadingTopics(false))
    MatchmakingService.getDebateStatus()
      .then(setStatus)
      .catch(() => {})
  }, [])

  useEffect(() => {
    const latestEvent = events[0]

    if (latestEvent?.eventType === websocketEvents.matchFound && latestEvent.payload?.sessionType === 'DEBATE') {
      MatchmakingService.getDebateStatus()
        .then(setStatus)
        .catch(() => {})
    }
  }, [events])

  async function joinQueue() {
    setError('')

    try {
      await MatchmakingService.joinDebateQueue(Number(topicId))
      setStatus(await MatchmakingService.getDebateStatus())
    } catch (exception) {
      setError(exception.message)
    }
  }

  async function leaveQueue() {
    await MatchmakingService.leaveDebateQueue()
    setStatus(await MatchmakingService.getDebateStatus())
  }

  return (
    <section className="page-stack">
      <header>
        <p className="eyebrow">Debate</p>
        <h1>Debate queue</h1>
      </header>
      <div className="toolbar">
        <select value={topicId} onChange={(event) => setTopicId(event.target.value)}>
          <option value="">Choose topic</option>
          {topics.map((topic) => (
            <option key={topic.id} value={topic.id}>
              {topic.title}
            </option>
          ))}
        </select>
        <button type="button" onClick={joinQueue} disabled={!topicId}>
          Join queue
        </button>
        <button type="button" className="ghost-button" onClick={leaveQueue}>
          Leave
        </button>
      </div>
      {loadingTopics && <LoadingBlock label="Loading topics" />}
      {!loadingTopics && topics.length === 0 && (
        <EmptyState title="No topics yet" message="Restart the backend to load demo topics, then refresh this page." />
      )}
      {!loadingTopics && topics.length > 0 && (
        <p className="muted">{topics.length} active debate topics available.</p>
      )}
      {matchedSessionId && (
        <Link to={`/debate/session/${matchedSessionId}`} className="inline-link">
          Open matched debate
        </Link>
      )}
      {status && <p className="muted">Queue size: {status.queueSize}</p>}
      {error && <p className="error-text">{error}</p>}
    </section>
  )
}

export default DebateLobbyPage
