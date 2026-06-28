import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import { websocketEvents } from '../../constants/websocket'
import { useWebSocketEvents } from '../../hooks/useWebSocketEvents'
import GDService from '../../services/GDService'

function GDLobbyPage() {
  const { events } = useWebSocketEvents()
  const [rooms, setRooms] = useState([])
  const [form, setForm] = useState({ topic: '', maxParticipants: 10 })
  const [error, setError] = useState('')
  const [loadingRooms, setLoadingRooms] = useState(true)

  async function loadRooms() {
    setRooms(await GDService.getActiveRooms())
  }

  useEffect(() => {
    GDService.getActiveRooms()
      .then(setRooms)
      .catch((exception) => setError(exception.message))
      .finally(() => setLoadingRooms(false))
  }, [])

  useEffect(() => {
    const latestEvent = events[0]

    if (
      latestEvent
      && [
        websocketEvents.gdRoomCreated,
        websocketEvents.userJoined,
        websocketEvents.userLeft,
        websocketEvents.gdRoomClosed,
      ].includes(latestEvent.eventType)
    ) {
      GDService.getActiveRooms()
        .then(setRooms)
        .catch((exception) => setError(exception.message))
    }
  }, [events])

  async function createRoom(event) {
    event.preventDefault()
    setError('')

    try {
      await GDService.createRoom({ ...form, maxParticipants: Number(form.maxParticipants) })
      await loadRooms()
      setForm({ topic: '', maxParticipants: 10 })
    } catch (exception) {
      setError(exception.message)
    }
  }

  return (
    <section className="page-stack">
      <header>
        <p className="eyebrow">Group Discussion</p>
        <h1>Active rooms</h1>
      </header>
      <form className="toolbar" onSubmit={createRoom}>
        <input
          type="text"
          placeholder="Topic"
          value={form.topic}
          onChange={(event) => setForm({ ...form, topic: event.target.value })}
          required
        />
        <input
          type="number"
          min="1"
          max="100"
          value={form.maxParticipants}
          onChange={(event) => setForm({ ...form, maxParticipants: event.target.value })}
          required
        />
        <button type="submit">Create</button>
      </form>
      {error && <p className="error-text">{error}</p>}
      {loadingRooms && <LoadingBlock label="Loading GD rooms" />}
      {!loadingRooms && rooms.length === 0 && (
        <EmptyState title="No active rooms" message="Create a room to start a group discussion." />
      )}
      <div className="list-grid">
        {rooms.map((room) => (
          <article key={room.sessionId}>
            <strong>{room.topic}</strong>
            <span>
              {room.currentParticipants}/{room.maxParticipants}
            </span>
            <Link to={`/gd/room/${room.sessionId}`}>Open room</Link>
          </article>
        ))}
      </div>
    </section>
  )
}

export default GDLobbyPage
