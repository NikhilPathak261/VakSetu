import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import DebateLobbyPage from './DebateLobbyPage'
import MatchmakingService from '../../services/MatchmakingService'
import TopicService from '../../services/TopicService'

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
    getDebateStatus: vi.fn(),
    joinDebateQueue: vi.fn(),
    leaveDebateQueue: vi.fn(),
  },
}))

vi.mock('../../services/TopicService', () => ({
  default: {
    getTopics: vi.fn(),
  },
}))

describe('DebateLobbyPage', () => {
  beforeEach(() => {
    websocketMocks.events = []
    MatchmakingService.getDebateStatus.mockReset()
    MatchmakingService.joinDebateQueue.mockReset()
    MatchmakingService.leaveDebateQueue.mockReset()
    TopicService.getTopics.mockReset()
  })

  it('renders debate topics, queue status, and matched session links', async () => {
    websocketMocks.events = [
      {
        eventType: 'MATCH_FOUND',
        payload: { sessionType: 'DEBATE', sessionId: 44 },
      },
    ]
    TopicService.getTopics.mockResolvedValue([
      { id: 1, title: 'Remote work' },
      { id: 2, title: 'AI education' },
    ])
    MatchmakingService.getDebateStatus.mockResolvedValue({ queueSize: 2 })

    render(
      <MemoryRouter>
        <DebateLobbyPage />
      </MemoryRouter>,
    )

    expect(await screen.findByText('2 active debate topics available.')).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Remote work' })).toBeInTheDocument()
    expect(screen.getByText('Queue size: 2')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /open matched debate/i })).toHaveAttribute(
      'href',
      '/debate/session/44',
    )
  })

  it('joins the debate queue with a numeric topic id and refreshes queue status', async () => {
    const user = userEvent.setup()
    TopicService.getTopics.mockResolvedValue([{ id: 7, title: 'Public transport' }])
    MatchmakingService.getDebateStatus
      .mockResolvedValueOnce({ queueSize: 0 })
      .mockResolvedValueOnce({ queueSize: 1 })
    MatchmakingService.joinDebateQueue.mockResolvedValue({ message: 'Joined debate queue' })

    render(
      <MemoryRouter>
        <DebateLobbyPage />
      </MemoryRouter>,
    )

    await screen.findByText('1 active debate topics available.')
    await user.selectOptions(screen.getByLabelText(/^topic$/i), '7')
    await user.click(screen.getByRole('button', { name: /join queue/i }))

    expect(MatchmakingService.joinDebateQueue).toHaveBeenCalledWith(7)
    expect(await screen.findByText('Queue size: 1')).toBeInTheDocument()
  })

  it('renders empty state when no topics are available', async () => {
    TopicService.getTopics.mockResolvedValue([])
    MatchmakingService.getDebateStatus.mockResolvedValue({ queueSize: 0 })

    render(
      <MemoryRouter>
        <DebateLobbyPage />
      </MemoryRouter>,
    )

    expect(await screen.findByText('No topics yet')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /join queue/i })).toBeDisabled()
  })

  it('surfaces topic and join failures through alerts', async () => {
    const user = userEvent.setup()
    TopicService.getTopics.mockResolvedValue([{ id: 7, title: 'Public transport' }])
    MatchmakingService.getDebateStatus.mockResolvedValue({ queueSize: 0 })
    MatchmakingService.joinDebateQueue.mockRejectedValue(new Error('Debate queue unavailable'))

    render(
      <MemoryRouter>
        <DebateLobbyPage />
      </MemoryRouter>,
    )

    await screen.findByText('1 active debate topics available.')
    await user.selectOptions(screen.getByLabelText(/^topic$/i), '7')
    await user.click(screen.getByRole('button', { name: /join queue/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Debate queue unavailable')
  })
})
