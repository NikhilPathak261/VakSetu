import { useWebSocketEvents } from '../../hooks/useWebSocketEvents'

function EventPanel() {
  const { connected, events, clearEvents } = useWebSocketEvents()

  return (
    <aside className="event-panel">
      <div className="event-panel-header">
        <div>
          <p className="eyebrow">Realtime</p>
          <strong>{connected ? 'Connected' : 'Disconnected'}</strong>
        </div>
        <button type="button" className="ghost-button compact-button" onClick={clearEvents}>
          Clear
        </button>
      </div>
      <div className="event-list">
        {events.length === 0 && <p className="muted">No events yet</p>}
        {events.map((event, index) => (
          <article key={`${event.eventType}-${event.timestamp}-${index}`}>
            <strong>{event.eventType}</strong>
            <span>{event.message}</span>
          </article>
        ))}
      </div>
    </aside>
  )
}

export default EventPanel
