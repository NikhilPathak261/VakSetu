import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
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
  let dateNowSpy

  beforeEach(() => {
    DebateService.getSession.mockReset()
    DebateService.startSession.mockReset()
    DebateService.startRoundOne.mockReset()
    DebateService.startRoundTwo.mockReset()
    DebateService.startRoundThree.mockReset()
    dateNowSpy = vi.spyOn(Date, 'now').mockReturnValue(new Date('2026-07-01T10:00:00Z').getTime())
  })

  afterEach(() => {
    dateNowSpy.mockRestore()
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

  it('starts preparation from a matched debate session', async () => {
    const user = userEvent.setup()
    DebateService.getSession.mockResolvedValue({
      id: 11,
      topicTitle: 'Remote work',
      status: 'MATCHED',
      currentRound: 0,
      totalRounds: 3,
      roundEndTime: null,
      participantAId: 1,
      participantAName: 'Sam',
      participantBId: 2,
      participantBName: 'Priya',
      sideA: 'FOR',
      sideB: 'AGAINST',
    })
    DebateService.startSession.mockResolvedValue({
      id: 11,
      topicTitle: 'Remote work',
      status: 'PREPARATION',
      currentRound: 0,
      totalRounds: 3,
      roundEndTime: '2026-07-01T10:05:00Z',
      participantAId: 1,
      participantAName: 'Sam',
      participantBId: 2,
      participantBName: 'Priya',
      sideA: 'FOR',
      sideB: 'AGAINST',
    })

    render(<DebateSessionPage />)

    await screen.findByText('Remote work')
    await user.click(screen.getByRole('button', { name: /start preparation/i }))

    expect(DebateService.startSession).toHaveBeenCalledWith('11')
    expect(await screen.findByText('PREPARATION')).toBeInTheDocument()
  })

  it('disables round advancement before the current window ends', async () => {
    DebateService.getSession.mockResolvedValue({
      id: 11,
      topicTitle: 'Remote work',
      status: 'PREPARATION',
      currentRound: 0,
      totalRounds: 3,
      roundEndTime: '2026-07-01T10:05:00Z',
      participantAId: 1,
      participantAName: 'Sam',
      participantBId: 2,
      participantBName: 'Priya',
      sideA: 'FOR',
      sideB: 'AGAINST',
    })

    render(<DebateSessionPage />)

    expect(await screen.findByRole('button', { name: /start round 1/i })).toBeDisabled()
  })

  it('starts round two after the active round window has ended', async () => {
    const user = userEvent.setup()
    const roundOneSession = {
      id: 11,
      topicTitle: 'Remote work',
      status: 'ROUND_1',
      currentRound: 1,
      totalRounds: 3,
      roundEndTime: '2026-07-01T09:59:00Z',
      participantAId: 1,
      participantAName: 'Sam',
      participantBId: 2,
      participantBName: 'Priya',
      sideA: 'FOR',
      sideB: 'AGAINST',
    }

    DebateService.getSession
      .mockResolvedValueOnce(roundOneSession)
      .mockResolvedValueOnce(roundOneSession)
      .mockResolvedValue({
        id: 11,
        topicTitle: 'Remote work',
        status: 'ROUND_2',
        currentRound: 2,
        totalRounds: 3,
        roundEndTime: '2026-07-01T10:05:00Z',
        participantAId: 1,
        participantAName: 'Sam',
        participantBId: 2,
        participantBName: 'Priya',
        sideA: 'FOR',
        sideB: 'AGAINST',
      })
    DebateService.startRoundTwo.mockResolvedValue({ message: 'Round two started' })

    render(<DebateSessionPage />)

    const button = await screen.findByRole('button', { name: /start round 2/i })
    expect(button).toBeEnabled()
    await user.click(button)

    expect(DebateService.startRoundTwo).toHaveBeenCalledWith('11')
    expect(await screen.findByText('ROUND_2')).toBeInTheDocument()
  })
})
