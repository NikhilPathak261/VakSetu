import { useAuth } from '../../hooks/useAuth'

function DashboardPage() {
  const { currentUser } = useAuth()

  return (
    <section className="page-stack">
      <header>
        <p className="eyebrow">Dashboard</p>
        <h1>{currentUser?.name || 'Your'} progress</h1>
      </header>
      <div className="stats-grid">
        <article>
          <span>Overall</span>
          <strong>{currentUser?.overallScore ?? '-'}</strong>
        </article>
        <article>
          <span>Rank</span>
          <strong>{currentUser?.rank ?? '-'}</strong>
        </article>
        <article>
          <span>Reputation</span>
          <strong>{currentUser?.reputation ?? '-'}</strong>
        </article>
        <article>
          <span>Badge</span>
          <strong>{currentUser?.contributorBadge ?? 'NONE'}</strong>
        </article>
      </div>
      <div className="skill-grid">
        {['fluency', 'pronunciation', 'grammar', 'confidence', 'empathy', 'listening', 'engagement'].map(
          (skill) => (
            <article key={skill}>
              <span>{skill}</span>
              <strong>{currentUser?.[skill] ?? '-'}</strong>
            </article>
          ),
        )}
      </div>
    </section>
  )
}

export default DashboardPage
