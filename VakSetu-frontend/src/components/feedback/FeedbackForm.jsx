import { useState } from 'react'
import FeedbackService from '../../services/FeedbackService'

const skills = [
  'fluency',
  'pronunciation',
  'grammar',
  'confidence',
  'empathy',
  'listening',
  'engagement',
]

function FeedbackForm({ sessionId, sessionType, targetUserId, targetName, onSubmitted }) {
  const [ratings, setRatings] = useState(() => Object.fromEntries(skills.map((skill) => [skill, 3])))
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    setError('')
    setLoading(true)

    try {
      const response = await FeedbackService.submitFeedback({
        sessionId,
        sessionType,
        targetUserId,
        fluencyRating: Number(ratings.fluency),
        pronunciationRating: Number(ratings.pronunciation),
        grammarRating: Number(ratings.grammar),
        confidenceRating: Number(ratings.confidence),
        empathyRating: Number(ratings.empathy),
        listeningRating: Number(ratings.listening),
        engagementRating: Number(ratings.engagement),
      })
      setMessage(response.message)
      onSubmitted?.(response)
    } catch (exception) {
      setError(exception.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <form className="form-card compact" onSubmit={handleSubmit}>
      <div>
        <p className="eyebrow">Feedback</p>
        <h2>{targetName ? `Rate ${targetName}` : 'Rate partner'}</h2>
      </div>
      <div className="rating-grid">
        {skills.map((skill) => (
          <label key={skill}>
            {skill}
            <select
              value={ratings[skill]}
              onChange={(event) => setRatings({ ...ratings, [skill]: event.target.value })}
            >
              <option value="1">1</option>
              <option value="2">2</option>
              <option value="3">3</option>
              <option value="4">4</option>
              <option value="5">5</option>
            </select>
          </label>
        ))}
      </div>
      {message && <p className="success-text">{message}</p>}
      {error && <p className="error-text">{error}</p>}
      <button type="submit" disabled={loading || !targetUserId}>
        {loading ? 'Submitting' : 'Submit feedback'}
      </button>
    </form>
  )
}

export default FeedbackForm
