import { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import FeedbackForm from '../../components/feedback/FeedbackForm'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
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
          <button type="button" onClick={() => advanceRound(DebateService.startRoundOne)}>
            Start round 1
          </button>
        )}
        {session?.status === 'ROUND_1' && (
          <button type="button" onClick={() => advanceRound(DebateService.startRoundTwo)}>
            Start round 2
          </button>
        )}
        {session?.status === 'ROUND_2' && (
          <button type="button" onClick={() => advanceRound(DebateService.startRoundThree)}>
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
      {error && <p className="error-text">{error}</p>}
    </section>
  )
}

export default DebateSessionPage
