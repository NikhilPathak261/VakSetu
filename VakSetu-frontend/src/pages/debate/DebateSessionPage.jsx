import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import FeedbackForm from '../../components/feedback/FeedbackForm'
import Alert from '../../components/common/Alert'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import SessionCountdown from '../../components/session/SessionCountdown'
import WebRtcCallPanel from '../../components/webrtc/WebRtcCallPanel'
import { useAuth } from '../../hooks/useAuth'
import DebateService from '../../services/DebateService'

function DebateSessionPage() {
  const { sessionId } = useParams()
  const { currentUser, refreshProfile } = useAuth()
  const [session, setSession] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [currentTime, setCurrentTime] = useState(0)
  const autoRefreshedWindowRef = useRef('')

  const loadSession = useCallback(async () => {
    setSession(await DebateService.getSession(sessionId))
  }, [sessionId])

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

  useEffect(() => {
    function updateCurrentTime() {
      setCurrentTime(Date.now())
    }

    updateCurrentTime()
    const intervalId = window.setInterval(updateCurrentTime, 1000)

    return () => window.clearInterval(intervalId)
  }, [])

  useEffect(() => {
    if (!session?.roundEndTime) {
      return
    }

    const windowKey = `${session.status}-${session.roundEndTime}`
    const windowEnded = new Date(session.roundEndTime).getTime() <= currentTime

    if (windowEnded && autoRefreshedWindowRef.current !== windowKey) {
      autoRefreshedWindowRef.current = windowKey
      loadSession().catch((exception) => setError(exception.message))
    }
  }, [currentTime, loadSession, session?.roundEndTime, session?.status])

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

  async function advanceRound(action) {
    setError('')

    try {
      await action(sessionId)
      await loadSession()
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

    return session.status === 'ROUND_3'
      && session.roundEndTime
      && new Date(session.roundEndTime).getTime() <= currentTime
  }, [currentTime, session])

  const activeWindowEnded = useMemo(() => {
    if (!session?.roundEndTime) {
      return false
    }

    return new Date(session.roundEndTime).getTime() <= currentTime
  }, [currentTime, session])

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
            <span>Round</span>
            <strong>{session.currentRound ?? 0}/{session.totalRounds ?? 3}</strong>
          </article>
          <SessionCountdown
            label={session.status === 'PREPARATION' ? 'Preparation' : 'Phase time'}
            targetTime={session.roundEndTime}
            currentTime={currentTime}
            onElapsedText="Ready"
          />
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
        {session?.status === 'MATCHED' && (
          <button type="button" onClick={startSession}>
            Start preparation
          </button>
        )}
        {session?.status === 'PREPARATION' && (
          <button type="button" onClick={() => advanceRound(DebateService.startRoundOne)} disabled={!activeWindowEnded}>
            Start round 1
          </button>
        )}
        {session?.status === 'ROUND_1' && (
          <button type="button" onClick={() => advanceRound(DebateService.startRoundTwo)} disabled={!activeWindowEnded}>
            Start round 2
          </button>
        )}
        {session?.status === 'ROUND_2' && (
          <button type="button" onClick={() => advanceRound(DebateService.startRoundThree)} disabled={!activeWindowEnded}>
            Start round 3
          </button>
        )}
        <button type="button" className="ghost-button" onClick={loadSession}>
          Refresh
        </button>
      </div>
      {partner && (
        <>
          <WebRtcCallPanel sessionId={sessionId} sessionType="DEBATE" partner={partner} />
          {feedbackReady ? (
            <FeedbackForm
              sessionId={Number(sessionId)}
              sessionType="DEBATE"
              targetUserId={partner.id}
              targetName={partner.name}
              onSubmitted={handleFeedbackSubmitted}
            />
          ) : (
            <p className="state-text">Feedback opens after round 3 ends.</p>
          )}
        </>
      )}
      <Alert variant="error">{error}</Alert>
    </section>
  )
}

export default DebateSessionPage
