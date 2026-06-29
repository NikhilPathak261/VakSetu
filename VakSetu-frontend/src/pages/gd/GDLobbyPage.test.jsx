import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import GDLobbyPage from './GDLobbyPage'
import GDService from '../../services/GDService'

vi.mock('../../hooks/useWebSocketEvents', () => ({
  useWebSocketEvents: () => ({
    events: [],
  }),
}))

vi.mock('../../services/GDService', () => ({
  default: {
    getActiveRooms: vi.fn(),
    createRoom: vi.fn(),
  },
}))

describe('GDLobbyPage', () => {
  beforeEach(() => {
    GDService.getActiveRooms.mockReset()
    GDService.createRoom.mockReset()
  })

  it('renders empty state when no active rooms are returned', async () => {
    GDService.getActiveRooms.mockResolvedValue([])

    render(
      <MemoryRouter>
        <GDLobbyPage />
      </MemoryRouter>,
    )

    expect(await screen.findByText('No active rooms')).toBeInTheDocument()
  })

  it('creates rooms with numeric max participants and refreshes the list', async () => {
    const user = userEvent.setup()
    GDService.getActiveRooms
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        { sessionId: 9, topic: 'Public speaking', currentParticipants: 1, maxParticipants: 6 },
      ])
    GDService.createRoom.mockResolvedValue({ sessionId: 9 })

    render(
      <MemoryRouter>
        <GDLobbyPage />
      </MemoryRouter>,
    )

    await screen.findByText('No active rooms')
    const maxParticipantsInput = screen.getByDisplayValue('10')

    await user.type(screen.getByPlaceholderText(/topic/i), 'Public speaking')
    await user.clear(maxParticipantsInput)
    await user.type(maxParticipantsInput, '6')
    await user.click(screen.getByRole('button', { name: /create/i }))

    expect(GDService.createRoom).toHaveBeenCalledWith({
      topic: 'Public speaking',
      maxParticipants: 6,
    })
    expect(await screen.findByText('Public speaking')).toBeInTheDocument()
  })

  it('surfaces room loading errors as alerts', async () => {
    GDService.getActiveRooms.mockRejectedValue(new Error('Rooms unavailable'))

    render(
      <MemoryRouter>
        <GDLobbyPage />
      </MemoryRouter>,
    )

    expect(await screen.findByRole('alert')).toHaveTextContent('Rooms unavailable')
  })
})
