import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import FeedbackForm from '../../components/feedback/FeedbackForm'
import Alert from '../../components/common/Alert'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import SessionCountdown from '../../components/session/SessionCountdown'
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
  const autoRefreshedWindowRef = useRef('')

  const loadSession = useCallback(async () => {
    setSession(await RoleplayService.getSession(sessionId))
  }, [sessionId])

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
    const intervalId = window.setInterval(updateCurrentTime, 1000)

    return () => window.clearInterval(intervalId)
  }, [])

  useEffect(() => {
    if (!session?.endTime) {
      return
    }

    const windowKey = `${session.status}-${session.endTime}`
    const windowEnded = new Date(session.endTime).getTime() <= currentTime

    if (windowEnded && autoRefreshedWindowRef.current !== windowKey) {
      autoRefreshedWindowRef.current = windowKey
      loadSession().catch((exception) => setError(exception.message))
    }
  }, [currentTime, loadSession, session?.endTime, session?.status])

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

  const activeWindowEnded = useMemo(() => {
    if (!session?.endTime) {
      return false
    }

    return new Date(session.endTime).getTime() <= currentTime
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
            <SessionCountdown
              label={session.status === 'PREPARATION' ? 'Preparation' : 'Session time'}
              targetTime={session.endTime}
              currentTime={currentTime}
              onElapsedText={session.status === 'PREPARATION' ? 'Ready' : 'Ended'}
            />
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
        {session?.status === 'PREPARATION' && (
          <button type="button" onClick={startRoleplay} disabled={!activeWindowEnded}>
            Start roleplay
          </button>
        )}
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
      <Alert variant="error">{error}</Alert>
    </section>
  )
}

export default RoleplaySessionPage
