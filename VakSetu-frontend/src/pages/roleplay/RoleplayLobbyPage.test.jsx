import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import RoleplayLobbyPage from './RoleplayLobbyPage'
import MatchmakingService from '../../services/MatchmakingService'

const websocketMocks = vi.hoisted(() => ({
  events: [],
}))

vi.mock('../../hooks/useWebSocketEvents', () => ({
  useWebSocketEvents: () => ({
    events: websocketMocks.events,
  }),
}))

vi.mock('../../services/MatchmakingService', () => ({
  default: {
    getRoleplayStatus: vi.fn(),
    joinRoleplayQueue: vi.fn(),
    leaveRoleplayQueue: vi.fn(),
  },
}))

describe('RoleplayLobbyPage', () => {
  beforeEach(() => {
    websocketMocks.events = []
    MatchmakingService.getRoleplayStatus.mockReset()
    MatchmakingService.joinRoleplayQueue.mockReset()
    MatchmakingService.leaveRoleplayQueue.mockReset()
  })

  it('renders queue status and matched session links from backend/realtime state', async () => {
    websocketMocks.events = [
      {
        eventType: 'MATCH_FOUND',
        payload: { sessionType: 'ROLEPLAY', sessionId: 22 },
      },
    ]
    MatchmakingService.getRoleplayStatus.mockResolvedValue({ queueSize: 3 })

    render(
      <MemoryRouter>
        <RoleplayLobbyPage />
      </MemoryRouter>,
    )

    expect(await screen.findByText('Queue size: 3')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /open matched roleplay/i })).toHaveAttribute(
      'href',
      '/roleplay/session/22',
    )
  })

  it('shows join errors through alert feedback', async () => {
    const user = userEvent.setup()
    MatchmakingService.getRoleplayStatus.mockResolvedValue({ queueSize: 0 })
    MatchmakingService.joinRoleplayQueue.mockRejectedValue(new Error('Queue unavailable'))

    render(
      <MemoryRouter>
        <RoleplayLobbyPage />
      </MemoryRouter>,
    )

    await screen.findByText('Queue size: 0')
    await user.click(screen.getByRole('button', { name: /join queue/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Queue unavailable')
  })
})
