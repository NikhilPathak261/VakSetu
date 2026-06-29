import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import GDRoomPage from './GDRoomPage'
import GDService from '../../services/GDService'

vi.mock('react-router-dom', () => ({
  useParams: () => ({ sessionId: '14' }),
}))

vi.mock('../../hooks/useWebSocketEvents', () => ({
  useWebSocketEvents: () => ({
    events: [],
  }),
}))

vi.mock('../../services/GDService', () => ({
  default: {
    getRoom: vi.fn(),
    getLeaderboard: vi.fn(),
    markSpoken: vi.fn(),
    giveStar: vi.fn(),
    closeRoom: vi.fn(),
  },
}))

describe('GDRoomPage', () => {
  beforeEach(() => {
    GDService.getRoom.mockReset()
    GDService.getLeaderboard.mockReset()
    GDService.markSpoken.mockReset()
    GDService.giveStar.mockReset()
    GDService.closeRoom.mockReset()
  })

  it('renders room details and leaderboard from backend state', async () => {
    GDService.getRoom.mockResolvedValue({
      sessionId: 14,
      topic: 'AI in schools',
      status: 'ACTIVE',
      currentParticipants: 4,
      maxParticipants: 10,
      creatorName: 'Sam',
    })
    GDService.getLeaderboard.mockResolvedValue({
      leaderboard: [{ userId: 2, userName: 'Priya', stars: 3 }],
    })

    render(<GDRoomPage />)

    expect(await screen.findByText('AI in schools')).toBeInTheDocument()
    expect(screen.getByText('4/10')).toBeInTheDocument()
    expect(screen.getByText('Priya')).toBeInTheDocument()
    expect(screen.getByText('3 stars')).toBeInTheDocument()
  })

  it('marks the authenticated participant as spoken and shows success feedback', async () => {
    const user = userEvent.setup()
    GDService.getRoom.mockResolvedValue({
      sessionId: 14,
      topic: 'AI in schools',
      status: 'ACTIVE',
      currentParticipants: 4,
      maxParticipants: 10,
      creatorName: 'Sam',
    })
    GDService.getLeaderboard.mockResolvedValue({ leaderboard: [] })
    GDService.markSpoken.mockResolvedValue({ message: 'Marked as spoken' })

    render(<GDRoomPage />)

    await screen.findByText('AI in schools')
    await user.click(screen.getByRole('button', { name: /mark spoken/i }))

    expect(GDService.markSpoken).toHaveBeenCalledWith('14')
    expect(await screen.findByRole('status')).toHaveTextContent('Marked as spoken')
  })
})
