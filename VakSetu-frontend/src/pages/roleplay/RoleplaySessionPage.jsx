import { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import FeedbackForm from '../../components/feedback/FeedbackForm'
import { useAuth } from '../../hooks/useAuth'
import RoleplayService from '../../services/RoleplayService'

function RoleplaySessionPage() {
  const { sessionId } = useParams()
  const { currentUser } = useAuth()
  const [session, setSession] = useState(null)
  const [error, setError] = useState('')

  async function loadSession() {
    setSession(await RoleplayService.getSession(sessionId))
  }

  useEffect(() => {
    RoleplayService.getSession(sessionId)
      .then(setSession)
      .catch((exception) => setError(exception.message))
  }, [sessionId])

  const partner = useMemo(() => {
    if (!session || !currentUser) {
      return null
    }

    if (currentUser.id === session.participantAId) {
      return { id: session.participantBId, name: session.participantBName }
    }

    if (currentUser.id === session.participantBId) {
      return { id: session.participantAId, name: session.participantAName }
    }

    return null
  }, [currentUser, session])

  async function startRoleplay() {
    setError('')

    try {
      setSession(await RoleplayService.startRoleplay(sessionId))
    } catch (exception) {
      setError(exception.message)
    }
  }

  return (
    <section className="page-stack">
      <header>
        <p className="eyebrow">Roleplay Session</p>
        <h1>{session?.scenarioTitle || 'Loading roleplay'}</h1>
      </header>
      {session && (
        <>
          <p className="muted">{session.scenarioDescription}</p>
          <div className="detail-grid">
            <article>
              <span>Status</span>
              <strong>{session.status}</strong>
            </article>
            <article>
              <span>{session.participantAName}</span>
              <strong>{session.assignedRoleA}</strong>
            </article>
            <article>
              <span>{session.participantBName}</span>
              <strong>{session.assignedRoleB}</strong>
            </article>
          </div>
        </>
      )}
      <div className="toolbar">
        <button type="button" onClick={startRoleplay}>
          Start roleplay
        </button>
        <button type="button" className="ghost-button" onClick={loadSession}>
          Refresh
        </button>
      </div>
      {partner && (
        <FeedbackForm
          sessionId={Number(sessionId)}
          sessionType="ROLEPLAY"
          targetUserId={partner.id}
          targetName={partner.name}
          onSubmitted={loadSession}
        />
      )}
      {error && <p className="error-text">{error}</p>}
    </section>
  )
}

export default RoleplaySessionPage
