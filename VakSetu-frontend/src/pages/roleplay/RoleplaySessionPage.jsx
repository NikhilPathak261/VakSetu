import { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import FeedbackForm from '../../components/feedback/FeedbackForm'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import WebRtcCallPanel from '../../components/webrtc/WebRtcCallPanel'
import { useAuth } from '../../hooks/useAuth'
import RoleplayService from '../../services/RoleplayService'

function RoleplaySessionPage() {
  const { sessionId } = useParams()
  const { currentUser, refreshProfile } = useAuth()
  const [session, setSession] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [currentTime, setCurrentTime] = useState(0)

  async function loadSession() {
    setSession(await RoleplayService.getSession(sessionId))
  }

  async function handleFeedbackSubmitted() {
    await loadSession()
    await refreshProfile()
  }

  useEffect(() => {
    RoleplayService.getSession(sessionId)
      .then(setSession)
      .catch((exception) => setError(exception.message))
      .finally(() => setLoading(false))
  }, [sessionId])

  useEffect(() => {
    function updateCurrentTime() {
      setCurrentTime(Date.now())
    }

    updateCurrentTime()
    const intervalId = window.setInterval(updateCurrentTime, 30000)

    return () => window.clearInterval(intervalId)
  }, [])

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

  const feedbackReady = useMemo(() => {
    if (!session) {
      return false
    }

    if (session.status === 'COMPLETED') {
      return true
    }

    return session.status === 'ACTIVE'
      && session.endTime
      && new Date(session.endTime).getTime() <= currentTime
  }, [currentTime, session])

  return (
    <section className="page-stack">
      <header>
        <p className="eyebrow">Roleplay Session</p>
        <h1>{session?.scenarioTitle || 'Loading roleplay'}</h1>
      </header>
      {loading && <LoadingBlock label="Loading roleplay session" />}
      {!loading && !session && (
        <EmptyState title="Roleplay not found" message="Check the session link or wait for matchmaking." />
      )}
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
        <>
          <WebRtcCallPanel sessionId={sessionId} sessionType="ROLEPLAY" partner={partner} />
          {feedbackReady ? (
            <FeedbackForm
              sessionId={Number(sessionId)}
              sessionType="ROLEPLAY"
              targetUserId={partner.id}
              targetName={partner.name}
              onSubmitted={handleFeedbackSubmitted}
            />
          ) : (
            <p className="state-text">Feedback opens after the roleplay session ends.</p>
          )}
        </>
      )}
      {error && <p className="error-text">{error}</p>}
    </section>
  )
}

export default RoleplaySessionPage
