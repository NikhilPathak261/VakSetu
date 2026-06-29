import { render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import DashboardPage from './DashboardPage'
import DashboardService from '../../services/DashboardService'

const authMocks = vi.hoisted(() => ({
  refreshProfile: vi.fn(),
}))

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    authLoading: false,
    currentUser: {
      id: 1,
      name: 'Sam',
      overallScore: 65,
      rank: 'BRONZE',
      reputation: 20,
      contributorBadge: 'NONE',
    },
    refreshProfile: authMocks.refreshProfile,
  }),
}))

vi.mock('../../services/DashboardService', () => ({
  default: {
    getSummary: vi.fn(),
    getSkillHistory: vi.fn(),
    getReputationHistory: vi.fn(),
  },
}))

describe('DashboardPage', () => {
  beforeEach(() => {
    DashboardService.getSummary.mockReset()
    DashboardService.getSkillHistory.mockReset()
    DashboardService.getReputationHistory.mockReset()
    authMocks.refreshProfile.mockReset()
  })

  it('renders backend-owned dashboard summary and histories', async () => {
    DashboardService.getSummary.mockResolvedValue({
      overallScore: 78,
      rank: 'SILVER',
      reputation: 32,
      contributorBadge: 'HELPER',
      skills: {
        fluency: 81,
        pronunciation: 72,
        grammar: 76,
        confidence: 80,
        empathy: 70,
        listening: 74,
        engagement: 79,
      },
      statistics: {
        completedDebates: 2,
        completedRoleplays: 1,
      },
    })
    DashboardService.getSkillHistory.mockResolvedValue([
      {
        id: 1,
        skillName: 'fluency',
        oldValue: 70,
        newValue: 81,
        sessionType: 'DEBATE',
        sessionId: 9,
      },
    ])
    DashboardService.getReputationHistory.mockResolvedValue([
      {
        id: 1,
        changeAmount: 5,
        reason: 'DEBATE_COMPLETED',
        createdAt: '2026-06-29T10:00:00',
      },
    ])

    render(<DashboardPage />)

    expect(await screen.findByText('Sam progress')).toBeInTheDocument()
    expect(screen.getByText('SILVER')).toBeInTheDocument()
    expect(screen.getByText('HELPER')).toBeInTheDocument()
    expect(screen.getByText('Skill trajectory')).toBeInTheDocument()
    expect(screen.getByText('DEBATE session #9')).toBeInTheDocument()
    expect(screen.getByText('DEBATE_COMPLETED')).toBeInTheDocument()

    await waitFor(() => {
      expect(DashboardService.getSummary).toHaveBeenCalledTimes(1)
    })
  })

  it('surfaces dashboard API failures as alerts', async () => {
    DashboardService.getSummary.mockRejectedValue(new Error('Dashboard unavailable'))
    DashboardService.getSkillHistory.mockResolvedValue([])
    DashboardService.getReputationHistory.mockResolvedValue([])

    render(<DashboardPage />)

    expect(await screen.findByRole('alert')).toHaveTextContent('Dashboard unavailable')
  })
})
