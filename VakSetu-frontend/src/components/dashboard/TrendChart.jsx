function TrendChart({ title, points, emptyMessage }) {
  const normalizedPoints = points.slice(-12)
  const values = normalizedPoints.map((point) => point.value)
  const min = values.length ? Math.min(...values) : 0
  const max = values.length ? Math.max(...values) : 100
  const spread = Math.max(max - min, 1)
  const path = normalizedPoints
    .map((point, index) => {
      const x = normalizedPoints.length === 1 ? 50 : (index / (normalizedPoints.length - 1)) * 100
      const y = 90 - ((point.value - min) / spread) * 80
      return `${index === 0 ? 'M' : 'L'} ${x} ${y}`
    })
    .join(' ')

  return (
    <article className="chart-card">
      <header>
        <span>{title}</span>
        {values.length > 0 && <strong>{values[values.length - 1]}</strong>}
      </header>
      {normalizedPoints.length === 0 ? (
        <p className="muted">{emptyMessage}</p>
      ) : (
        <svg viewBox="0 0 100 100" role="img" aria-label={title}>
          <path d="M 0 90 H 100" className="chart-axis" />
          <path d={path} className="chart-line" />
          {normalizedPoints.map((point, index) => {
            const x = normalizedPoints.length === 1 ? 50 : (index / (normalizedPoints.length - 1)) * 100
            const y = 90 - ((point.value - min) / spread) * 80

            return <circle key={`${point.label}-${index}`} cx={x} cy={y} r="2.5" className="chart-dot" />
          })}
        </svg>
      )}
    </article>
  )
}

export default TrendChart
