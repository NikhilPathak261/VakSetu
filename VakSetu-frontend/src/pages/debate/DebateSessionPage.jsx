import { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import FeedbackForm from '../../components/feedback/FeedbackForm'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import WebRtcSignalingPanel from '../../components/webrtc/WebRtcSignalingPanel'
import { useAuth } from '../../hooks/useAuth'
import DebateService from '../../services/DebateService'

function DebateSessionPage() {
  const { sessionId } = useParams()
  const { currentUser, refreshProfile } = useAuth()
  const [session, setSession] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  async function loadSession() {
    setSession(await DebateService.getSession(sessionId))
  }

  async function handleFeedbackSubmitted() {
    await loadSession()
    await refreshProfile()
  }

  useEffect(() => {
    DebateService.getSession(sessionId)
      .then(setSession)
      .catch((exception) => setError(exception.message))
      .finally(() => setLoading(false))
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

  async function startSession() {
    setError('')

    try {
      setSession(await DebateService.startSession(sessionId))
    } catch (exception) {
      setError(exception.message)
    }
  }

  return (
    <section className="page-stack">
      <header>
        <p className="eyebrow">Debate Session</p>
        <h1>{session?.topicTitle || 'Loading debate'}</h1>
      </header>
      {loading && <LoadingBlock label="Loading debate session" />}
      {!loading && !session && (
        <EmptyState title="Debate not found" message="Check the session link or wait for matchmaking." />
      )}
      {session && (
        <div className="detail-grid">
          <article>
            <span>Status</span>
            <strong>{session.status}</strong>
          </article>
          <article>
            <span>{session.participantAName}</span>
            <strong>{session.sideA}</strong>
          </article>
          <article>
            <span>{session.participantBName}</span>
            <strong>{session.sideB}</strong>
          </article>
        </div>
      )}
      <div className="toolbar">
        <button type="button" onClick={startSession}>
          Start debate
        </button>
        <button type="button" className="ghost-button" onClick={loadSession}>
          Refresh
        </button>
      </div>
      {partner && (
        <>
          <WebRtcSignalingPanel sessionId={sessionId} sessionType="DEBATE" partner={partner} />
          <FeedbackForm
            sessionId={Number(sessionId)}
            sessionType="DEBATE"
            targetUserId={partner.id}
            targetName={partner.name}
            onSubmitted={handleFeedbackSubmitted}
          />
        </>
      )}
      {error && <p className="error-text">{error}</p>}
    </section>
  )
}

export default DebateSessionPage
