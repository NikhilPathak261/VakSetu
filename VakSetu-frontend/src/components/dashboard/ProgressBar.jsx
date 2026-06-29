function ProgressBar({ label, value, max = 100 }) {
  const safeValue = Number.isFinite(Number(value)) ? Number(value) : 0
  const percent = Math.max(0, Math.min(100, (safeValue / max) * 100))

  return (
    <article className="progress-row">
      <div>
        <span>{label}</span>
        <strong>{safeValue.toFixed(1)}</strong>
      </div>
      <div className="progress-track" aria-hidden="true">
        <span style={{ width: `${percent}%` }} />
      </div>
    </article>
  )
}

export default ProgressBar
