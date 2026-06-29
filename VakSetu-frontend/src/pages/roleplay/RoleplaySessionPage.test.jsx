import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import RoleplaySessionPage from './RoleplaySessionPage'
import RoleplayService from '../../services/RoleplayService'

vi.mock('react-router-dom', () => ({
  useParams: () => ({ sessionId: '33' }),
}))

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    currentUser: { id: 1, name: 'Sam' },
    refreshProfile: vi.fn(),
  }),
}))

vi.mock('../../services/RoleplayService', () => ({
  default: {
    getSession: vi.fn(),
    startRoleplay: vi.fn(),
  },
}))

vi.mock('../../components/webrtc/WebRtcCallPanel', () => ({
  default: () => <section>WebRTC call panel</section>,
}))

describe('RoleplaySessionPage', () => {
  beforeEach(() => {
    RoleplayService.getSession.mockReset()
    RoleplayService.startRoleplay.mockReset()
  })

  it('keeps feedback closed before the roleplay activity window ends', async () => {
    RoleplayService.getSession.mockResolvedValue({
      id: 33,
      scenarioTitle: 'Hotel check-in',
      scenarioDescription: 'Practice a hotel desk conversation.',
      status: 'ACTIVE',
      endTime: '2099-01-01T00:00:00',
      participantAId: 1,
      participantAName: 'Sam',
      participantBId: 2,
      participantBName: 'Priya',
      assignedRoleA: 'Guest',
      assignedRoleB: 'Receptionist',
    })

    render(<RoleplaySessionPage />)

    expect(await screen.findByText('Hotel check-in')).toBeInTheDocument()
    expect(screen.getByText('Feedback opens after the roleplay session ends.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /submit feedback/i })).not.toBeInTheDocument()
  })

  it('opens feedback when the roleplay session is completed', async () => {
    RoleplayService.getSession.mockResolvedValue({
      id: 33,
      scenarioTitle: 'Hotel check-in',
      scenarioDescription: 'Practice a hotel desk conversation.',
      status: 'COMPLETED',
      endTime: '2026-06-29T10:00:00',
      participantAId: 1,
      participantAName: 'Sam',
      participantBId: 2,
      participantBName: 'Priya',
      assignedRoleA: 'Guest',
      assignedRoleB: 'Receptionist',
    })

    render(<RoleplaySessionPage />)

    expect(await screen.findByText('Rate Priya')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /submit feedback/i })).toBeInTheDocument()
  })
})
