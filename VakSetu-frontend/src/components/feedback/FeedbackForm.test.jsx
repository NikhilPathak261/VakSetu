import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import FeedbackForm from './FeedbackForm'
import FeedbackService from '../../services/FeedbackService'

vi.mock('../../services/FeedbackService', () => ({
  default: {
    submitFeedback: vi.fn(),
  },
}))

describe('FeedbackForm', () => {
  beforeEach(() => {
    FeedbackService.submitFeedback.mockReset()
  })

  it('submits the backend feedback DTO and shows success feedback', async () => {
    const user = userEvent.setup()
    const onSubmitted = vi.fn()
    FeedbackService.submitFeedback.mockResolvedValue({ message: 'Feedback submitted' })

    render(
      <FeedbackForm
        sessionId={42}
        sessionType="DEBATE"
        targetUserId={7}
        targetName="Priya"
        onSubmitted={onSubmitted}
      />,
    )

    await user.selectOptions(screen.getByLabelText(/fluency/i), '5')
    await user.selectOptions(screen.getByLabelText(/grammar/i), '4')
    await user.click(screen.getByRole('button', { name: /submit feedback/i }))

    expect(FeedbackService.submitFeedback).toHaveBeenCalledWith({
      sessionId: 42,
      sessionType: 'DEBATE',
      targetUserId: 7,
      fluencyRating: 5,
      pronunciationRating: 3,
      grammarRating: 4,
      confidenceRating: 3,
      empathyRating: 3,
      listeningRating: 3,
      engagementRating: 3,
    })
    expect(await screen.findByRole('status')).toHaveTextContent('Feedback submitted')
    expect(onSubmitted).toHaveBeenCalledWith({ message: 'Feedback submitted' })
  })
})
