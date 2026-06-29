import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import DebateSessionPage from './DebateSessionPage'
import DebateService from '../../services/DebateService'

vi.mock('react-router-dom', () => ({
  useParams: () => ({ sessionId: '11' }),
}))

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    currentUser: { id: 1, name: 'Sam' },
    refreshProfile: vi.fn(),
  }),
}))

vi.mock('../../services/DebateService', () => ({
  default: {
    getSession: vi.fn(),
    startSession: vi.fn(),
    startRoundOne: vi.fn(),
    startRoundTwo: vi.fn(),
    startRoundThree: vi.fn(),
  },
}))

vi.mock('../../components/webrtc/WebRtcCallPanel', () => ({
  default: () => <section>WebRTC call panel</section>,
}))

describe('DebateSessionPage', () => {
  beforeEach(() => {
    DebateService.getSession.mockReset()
  })

  it('keeps feedback closed until the final debate round is eligible', async () => {
    DebateService.getSession.mockResolvedValue({
      id: 11,
      topicTitle: 'Remote work',
      status: 'ROUND_2',
      currentRound: 2,
      totalRounds: 3,
      roundEndTime: '2099-01-01T00:00:00',
      participantAId: 1,
      participantAName: 'Sam',
      participantBId: 2,
      participantBName: 'Priya',
      sideA: 'FOR',
      sideB: 'AGAINST',
    })

    render(<DebateSessionPage />)

    expect(await screen.findByText('Remote work')).toBeInTheDocument()
    expect(screen.getByText('Feedback opens after round 3 ends.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /submit feedback/i })).not.toBeInTheDocument()
  })
})
