function ProgressSummary({ skillHistory, reputationHistory }) {
  const latestSkillChanges = skillHistory.slice(0, 7)
  const skillDelta = latestSkillChanges.reduce((total, entry) => total + (entry.newValue - entry.oldValue), 0)
  const reputationDelta = reputationHistory
    .slice(0, 10)
    .reduce((total, entry) => total + entry.changeAmount, 0)

  return (
    <div className="stats-grid">
      <article>
        <span>Recent skill movement</span>
        <strong>{formatSigned(skillDelta)}</strong>
      </article>
      <article>
        <span>Recent reputation</span>
        <strong>{formatSigned(reputationDelta)}</strong>
      </article>
      <article>
        <span>Skill updates</span>
        <strong>{skillHistory.length}</strong>
      </article>
      <article>
        <span>Reputation events</span>
        <strong>{reputationHistory.length}</strong>
      </article>
    </div>
  )
}

function formatSigned(value) {
  const roundedValue = Math.round(value * 10) / 10
  return roundedValue > 0 ? `+${roundedValue}` : `${roundedValue}`
}

export default ProgressSummary
