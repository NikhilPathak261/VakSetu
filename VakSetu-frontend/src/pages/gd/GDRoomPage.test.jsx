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
    joinRoom: vi.fn(),
    leaveRoom: vi.fn(),
    markSpoken: vi.fn(),
    giveStar: vi.fn(),
    closeRoom: vi.fn(),
  },
}))

describe('GDRoomPage', () => {
  beforeEach(() => {
    GDService.getRoom.mockReset()
    GDService.getLeaderboard.mockReset()
    GDService.joinRoom.mockReset()
    GDService.leaveRoom.mockReset()
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

  it('joins and leaves rooms through backend-owned participation endpoints', async () => {
    const user = userEvent.setup()
    GDService.getRoom
      .mockResolvedValueOnce({
        sessionId: 14,
        topic: 'AI in schools',
        status: 'ACTIVE',
        currentParticipants: 4,
        maxParticipants: 10,
        creatorName: 'Sam',
      })
      .mockResolvedValueOnce({
        sessionId: 14,
        topic: 'AI in schools',
        status: 'ACTIVE',
        currentParticipants: 5,
        maxParticipants: 10,
        creatorName: 'Sam',
      })
      .mockResolvedValueOnce({
        sessionId: 14,
        topic: 'AI in schools',
        status: 'ACTIVE',
        currentParticipants: 4,
        maxParticipants: 10,
        creatorName: 'Sam',
      })
    GDService.getLeaderboard.mockResolvedValue({ leaderboard: [] })
    GDService.joinRoom.mockResolvedValue({ message: 'Joined room' })
    GDService.leaveRoom.mockResolvedValue({ message: 'Left room' })

    render(<GDRoomPage />)

    await screen.findByText('AI in schools')
    await user.click(screen.getByRole('button', { name: /join room/i }))

    expect(GDService.joinRoom).toHaveBeenCalledWith('14')
    expect(await screen.findByRole('status')).toHaveTextContent('Joined room')
    expect(await screen.findByText('5/10')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /leave room/i }))

    expect(GDService.leaveRoom).toHaveBeenCalledWith('14')
    expect(await screen.findByRole('status')).toHaveTextContent('Left room')
    expect(await screen.findByText('4/10')).toBeInTheDocument()
  })

  it('disables room actions after the room is completed', async () => {
    GDService.getRoom.mockResolvedValue({
      sessionId: 14,
      topic: 'AI in schools',
      status: 'COMPLETED',
      currentParticipants: 4,
      maxParticipants: 10,
      creatorName: 'Sam',
    })
    GDService.getLeaderboard.mockResolvedValue({ leaderboard: [] })

    render(<GDRoomPage />)

    await screen.findByText('AI in schools')

    expect(screen.getByRole('button', { name: /join room/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /leave room/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /mark spoken/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /give star/i })).toBeDisabled()
  })

  it('gives stars with the backend DTO and refreshes leaderboard state', async () => {
    const user = userEvent.setup()
    GDService.getRoom.mockResolvedValue({
      sessionId: 14,
      topic: 'AI in schools',
      status: 'ACTIVE',
      currentParticipants: 4,
      maxParticipants: 10,
      creatorName: 'Sam',
    })
    GDService.getLeaderboard
      .mockResolvedValueOnce({ leaderboard: [] })
      .mockResolvedValueOnce({ leaderboard: [{ userId: 2, userName: 'Priya', stars: 1 }] })
    GDService.giveStar.mockResolvedValue({ message: 'Star given' })

    render(<GDRoomPage />)

    await screen.findByText('AI in schools')
    await user.type(screen.getByPlaceholderText(/receiver user id/i), '2')
    await user.click(screen.getByRole('button', { name: /give star/i }))

    expect(GDService.giveStar).toHaveBeenCalledWith({
      sessionId: 14,
      receiverId: 2,
    })
    expect(await screen.findByRole('status')).toHaveTextContent('Star given')
    expect(await screen.findByText('Priya')).toBeInTheDocument()
  })

  it('closes the room and shows success feedback', async () => {
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
    GDService.closeRoom.mockResolvedValue({
      sessionId: 14,
      topic: 'AI in schools',
      status: 'COMPLETED',
      currentParticipants: 4,
      maxParticipants: 10,
      creatorName: 'Sam',
    })

    render(<GDRoomPage />)

    await screen.findByText('AI in schools')
    await user.click(screen.getByRole('button', { name: /close room/i }))

    expect(GDService.closeRoom).toHaveBeenCalledWith('14')
    expect(await screen.findByRole('status')).toHaveTextContent('Room closed')
    expect(screen.getByText('COMPLETED')).toBeInTheDocument()
  })
})
