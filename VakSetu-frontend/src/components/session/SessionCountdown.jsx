function SessionCountdown({ label, targetTime, currentTime, onElapsedText = 'Ready' }) {
  if (!targetTime) {
    return null
  }

  const remainingMs = Math.max(0, new Date(targetTime).getTime() - currentTime)
  const totalSeconds = Math.ceil(remainingMs / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  const displayTime = `${minutes}:${seconds.toString().padStart(2, '0')}`

  return (
    <article className="countdown-card">
      <span>{label}</span>
      <strong>{remainingMs === 0 ? onElapsedText : displayTime}</strong>
    </article>
  )
}

export default SessionCountdown
