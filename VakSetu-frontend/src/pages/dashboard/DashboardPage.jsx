import { useEffect, useState } from 'react'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import ProgressBar from '../../components/dashboard/ProgressBar'
import ProgressSummary from '../../components/dashboard/ProgressSummary'
import TrendChart from '../../components/dashboard/TrendChart'
import { useAuth } from '../../hooks/useAuth'
import DashboardService from '../../services/DashboardService'

const SKILLS = ['fluency', 'pronunciation', 'grammar', 'confidence', 'empathy', 'listening', 'engagement']

function DashboardPage() {
  const { authLoading, currentUser, refreshProfile } = useAuth()
  const [summary, setSummary] = useState(null)
  const [skillHistory, setSkillHistory] = useState([])
  const [reputationHistory, setReputationHistory] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!authLoading && currentUser) {
      loadDashboard()
    }
  }, [authLoading, currentUser])

  async function loadDashboard() {
    setLoading(true)
    setError('')

    try {
      const [summaryData, skillHistoryData, reputationHistoryData] = await Promise.all([
        DashboardService.getSummary(),
        DashboardService.getSkillHistory(),
        DashboardService.getReputationHistory(),
      ])

      setSummary(summaryData)
      setSkillHistory(skillHistoryData)
      setReputationHistory(reputationHistoryData)
    } catch (exception) {
      setError(exception.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleRefresh() {
    await Promise.all([refreshProfile(), loadDashboard()])
  }

  if (authLoading) {
    return <LoadingBlock label="Loading dashboard" />
  }

  if (!currentUser) {
    return <EmptyState title="Profile unavailable" message="Refresh your session and try again." />
  }

  return (
    <section className="page-stack">
      <header>
        <p className="eyebrow">Dashboard</p>
        <h1>{currentUser.name} progress</h1>
      </header>
      <div className="toolbar">
        <button type="button" className="ghost-button" onClick={handleRefresh} disabled={loading}>
          {loading ? 'Refreshing' : 'Refresh dashboard'}
        </button>
      </div>
      {error && <p className="error-text">{error}</p>}
      {loading && <LoadingBlock label="Loading progress data" />}
      <div className="stats-grid">
        <article>
          <span>Overall</span>
          <strong>{summary?.overallScore ?? currentUser.overallScore ?? '-'}</strong>
        </article>
        <article>
          <span>Rank</span>
          <strong>{summary?.rank ?? currentUser.rank ?? '-'}</strong>
        </article>
        <article>
          <span>Reputation</span>
          <strong>{summary?.reputation ?? currentUser.reputation ?? '-'}</strong>
        </article>
        <article>
          <span>Badge</span>
          <strong>{summary?.contributorBadge ?? currentUser.contributorBadge ?? 'NONE'}</strong>
        </article>
      </div>
      <div className="skill-grid">
        {SKILLS.map((skill) => (
          <article key={skill}>
            <span>{skill}</span>
            <strong>{summary?.skills?.[skill] ?? currentUser[skill] ?? '-'}</strong>
          </article>
        ))}
      </div>
      <section className="page-stack">
        <header>
          <p className="eyebrow">Skills</p>
          <h2>Current skill balance</h2>
        </header>
        <div className="progress-panel">
          {SKILLS.map((skill) => (
            <ProgressBar
              key={skill}
              label={formatLabel(skill)}
              value={summary?.skills?.[skill] ?? currentUser[skill] ?? 0}
            />
          ))}
        </div>
      </section>
      {summary?.statistics && (
        <section className="page-stack">
          <header>
            <p className="eyebrow">Activity</p>
            <h2>Session stats</h2>
          </header>
          <div className="stats-grid">
            {Object.entries(summary.statistics).map(([key, value]) => (
              <article key={key}>
                <span>{formatLabel(key)}</span>
                <strong>{value ?? 0}</strong>
              </article>
            ))}
          </div>
        </section>
      )}
      <section className="page-stack">
        <header>
          <p className="eyebrow">Trends</p>
          <h2>Recent progress</h2>
        </header>
        <ProgressSummary skillHistory={skillHistory} reputationHistory={reputationHistory} />
        <div className="chart-grid">
          <TrendChart
            title="Skill trajectory"
            points={buildSkillTrend(skillHistory)}
            emptyMessage="Complete sessions to see skill movement."
          />
          <TrendChart
            title="Reputation trajectory"
            points={buildReputationTrend(summary?.reputation, reputationHistory)}
            emptyMessage="Complete sessions to see reputation movement."
          />
        </div>
      </section>
      <section className="page-stack">
        <header>
          <p className="eyebrow">History</p>
          <h2>Skill changes</h2>
        </header>
        {skillHistory.length === 0 ? (
          <EmptyState title="No skill history yet" message="Complete Debate or Roleplay feedback to build history." />
        ) : (
          <div className="history-list">
            {skillHistory.map((entry) => (
              <article key={entry.id}>
                <div>
                  <strong>{formatLabel(entry.skillName)}</strong>
                  <span>{entry.sessionType} session #{entry.sessionId}</span>
                </div>
                <span>
                  {entry.oldValue} to {entry.newValue}
                </span>
              </article>
            ))}
          </div>
        )}
      </section>
      <section className="page-stack">
        <header>
          <p className="eyebrow">History</p>
          <h2>Reputation changes</h2>
        </header>
        {reputationHistory.length === 0 ? (
          <EmptyState title="No reputation history yet" message="Session completion rewards will appear here." />
        ) : (
          <div className="history-list">
            {reputationHistory.map((entry) => (
              <article key={entry.id}>
                <div>
                  <strong>{entry.changeAmount > 0 ? `+${entry.changeAmount}` : entry.changeAmount}</strong>
                  <span>{entry.reason}</span>
                </div>
                <span>{formatDate(entry.createdAt)}</span>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  )
}

function formatLabel(value) {
  return value.replace(/([A-Z])/g, ' $1').replace(/^./, (letter) => letter.toUpperCase())
}

function formatDate(value) {
  if (!value) {
    return '-'
  }

  return new Date(value).toLocaleString()
}

function buildSkillTrend(skillHistory) {
  return skillHistory
    .slice()
    .reverse()
    .map((entry) => ({
      label: `${entry.skillName}-${entry.id}`,
      value: Number(entry.newValue.toFixed(1)),
    }))
}

function buildReputationTrend(currentReputation, reputationHistory) {
  if (!Number.isFinite(Number(currentReputation))) {
    return []
  }

  let runningValue = Number(currentReputation) - reputationHistory
    .reduce((total, entry) => total + entry.changeAmount, 0)

  return reputationHistory.slice().reverse().map((entry) => {
    runningValue += entry.changeAmount
    return {
      label: `${entry.reason}-${entry.id}`,
      value: runningValue,
    }
  })
}

export default DashboardPage
