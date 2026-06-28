import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import { websocketEvents } from '../../constants/websocket'
import { useWebSocketEvents } from '../../hooks/useWebSocketEvents'
import GDService from '../../services/GDService'

function GDRoomPage() {
  const { sessionId } = useParams()
  const { events } = useWebSocketEvents()
  const [room, setRoom] = useState(null)
  const [leaderboard, setLeaderboard] = useState([])
  const [receiverId, setReceiverId] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  async function loadRoom() {
    const [roomResponse, leaderboardResponse] = await Promise.all([
      GDService.getRoom(sessionId),
      GDService.getLeaderboard(sessionId),
    ])
    setRoom(roomResponse)
    setLeaderboard(leaderboardResponse.leaderboard || [])
  }

  useEffect(() => {
    Promise.all([
      GDService.getRoom(sessionId),
      GDService.getLeaderboard(sessionId),
    ])
      .then(([roomResponse, leaderboardResponse]) => {
        setRoom(roomResponse)
        setLeaderboard(leaderboardResponse.leaderboard || [])
      })
      .catch((exception) => setError(exception.message))
      .finally(() => setLoading(false))
  }, [sessionId])

  useEffect(() => {
    const latestEvent = events[0]

    if (
      latestEvent?.sessionId === Number(sessionId)
      && [
        websocketEvents.userJoined,
        websocketEvents.userLeft,
        websocketEvents.starReceived,
        websocketEvents.leaderboardUpdated,
        websocketEvents.gdRoomClosed,
      ].includes(latestEvent.eventType)
    ) {
      Promise.all([
        GDService.getRoom(sessionId),
        GDService.getLeaderboard(sessionId),
      ])
        .then(([roomResponse, leaderboardResponse]) => {
          setRoom(roomResponse)
          setLeaderboard(leaderboardResponse.leaderboard || [])
        })
        .catch((exception) => setError(exception.message))
    }
  }, [events, sessionId])

  async function markSpoken() {
    setError('')
    setMessage('')

    try {
      const response = await GDService.markSpoken(sessionId)
      setMessage(response.message)
    } catch (exception) {
      setError(exception.message)
    }
  }

  async function giveStar(event) {
    event.preventDefault()
    setError('')
    setMessage('')

    try {
      const response = await GDService.giveStar({
        sessionId: Number(sessionId),
        receiverId: Number(receiverId),
      })
      setMessage(response.message)
      setReceiverId('')
      await loadRoom()
    } catch (exception) {
      setError(exception.message)
    }
  }

  async function closeRoom() {
    setError('')
    setMessage('')

    try {
      const response = await GDService.closeRoom(sessionId)
      setRoom(response)
      setMessage('Room closed')
    } catch (exception) {
      setError(exception.message)
    }
  }

  return (
    <section className="page-stack">
      <header>
        <p className="eyebrow">GD Room</p>
        <h1>{room?.topic || 'Loading room'}</h1>
      </header>
      {loading && <LoadingBlock label="Loading GD room" />}
      {room && (
        <div className="detail-grid">
          <article>
            <span>Status</span>
            <strong>{room.status}</strong>
          </article>
          <article>
            <span>Participants</span>
            <strong>
              {room.currentParticipants}/{room.maxParticipants}
            </strong>
          </article>
          <article>
            <span>Creator</span>
            <strong>{room.creatorName}</strong>
          </article>
        </div>
      )}
      <div className="toolbar">
        <button type="button" onClick={markSpoken}>
          Mark spoken
        </button>
        <button type="button" className="ghost-button" onClick={closeRoom}>
          Close room
        </button>
        <button type="button" className="ghost-button" onClick={loadRoom}>
          Refresh
        </button>
      </div>
      <form className="toolbar" onSubmit={giveStar}>
        <input
          type="number"
          min="1"
          placeholder="Receiver user id"
          value={receiverId}
          onChange={(event) => setReceiverId(event.target.value)}
          required
        />
        <button type="submit">Give star</button>
      </form>
      <div className="list-grid">
        {!loading && leaderboard.length === 0 && (
          <EmptyState title="No leaderboard yet" message="Stars will appear here after participants speak." />
        )}
        {leaderboard.map((entry) => (
          <article key={entry.userId}>
            <strong>{entry.userName}</strong>
            <span>{entry.stars} stars</span>
          </article>
        ))}
      </div>
      {message && <p className="success-text">{message}</p>}
      {error && <p className="error-text">{error}</p>}
    </section>
  )
}

export default GDRoomPage
