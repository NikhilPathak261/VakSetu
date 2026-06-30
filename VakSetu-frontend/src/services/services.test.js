import { beforeEach, describe, expect, it, vi } from 'vitest'
import api from './api'
import AuthService from './AuthService'
import DashboardService from './DashboardService'
import DebateService from './DebateService'
import FeedbackService from './FeedbackService'
import GDService from './GDService'
import MatchmakingService from './MatchmakingService'
import RoleplayService from './RoleplayService'
import TopicService from './TopicService'
import UserService from './UserService'

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
  },
}))

describe('frontend service wrappers', () => {
  beforeEach(() => {
    api.get.mockReset()
    api.post.mockReset()
    api.put.mockReset()
  })

  it('calls auth endpoints with expected payloads', async () => {
    api.post.mockResolvedValue({ data: { accessToken: 'token' } })

    await expect(AuthService.login({ email: 'sam@example.com', password: 'password123' })).resolves.toEqual({
      accessToken: 'token',
    })
    await AuthService.register({ name: 'Sam', email: 'sam@example.com', password: 'password123' })
    await AuthService.refresh('refresh-token')
    await AuthService.logout('refresh-token')

    expect(api.post).toHaveBeenNthCalledWith(1, '/auth/login', {
      email: 'sam@example.com',
      password: 'password123',
    })
    expect(api.post).toHaveBeenNthCalledWith(2, '/auth/register', {
      name: 'Sam',
      email: 'sam@example.com',
      password: 'password123',
    })
    expect(api.post).toHaveBeenNthCalledWith(3, '/auth/refresh', { refreshToken: 'refresh-token' })
    expect(api.post).toHaveBeenNthCalledWith(4, '/auth/logout', { refreshToken: 'refresh-token' })
  })

  it('calls dashboard read endpoints', async () => {
    api.get.mockResolvedValue({ data: [] })

    await DashboardService.getSummary()
    await DashboardService.getSkillHistory()
    await DashboardService.getReputationHistory()

    expect(api.get).toHaveBeenNthCalledWith(1, '/dashboard/me')
    expect(api.get).toHaveBeenNthCalledWith(2, '/dashboard/me/skill-history')
    expect(api.get).toHaveBeenNthCalledWith(3, '/dashboard/me/reputation-history')
  })

  it('calls user profile endpoints', async () => {
    api.get.mockResolvedValue({ data: { name: 'Sam' } })
    api.put.mockResolvedValue({ data: { name: 'Priya' } })

    await expect(UserService.getProfile()).resolves.toEqual({ name: 'Sam' })
    await expect(UserService.updateProfile({ name: 'Priya' })).resolves.toEqual({ name: 'Priya' })

    expect(api.get).toHaveBeenCalledWith('/users/me')
    expect(api.put).toHaveBeenCalledWith('/users/me', { name: 'Priya' })
  })

  it('calls topic endpoint', async () => {
    api.get.mockResolvedValue({ data: [{ id: 1, title: 'Remote work' }] })

    await expect(TopicService.getTopics()).resolves.toEqual([{ id: 1, title: 'Remote work' }])

    expect(api.get).toHaveBeenCalledWith('/topics')
  })

  it('calls matchmaking queue endpoints with expected payloads', async () => {
    api.get.mockResolvedValue({ data: { queueSize: 0 } })
    api.post.mockResolvedValue({ data: { message: 'ok' } })

    await MatchmakingService.joinDebateQueue(7)
    await MatchmakingService.leaveDebateQueue()
    await MatchmakingService.getDebateStatus()
    await MatchmakingService.joinRoleplayQueue()
    await MatchmakingService.leaveRoleplayQueue()
    await MatchmakingService.getRoleplayStatus()

    expect(api.post).toHaveBeenNthCalledWith(1, '/matchmaking/debate/join', { topicId: 7 })
    expect(api.post).toHaveBeenNthCalledWith(2, '/matchmaking/debate/leave')
    expect(api.get).toHaveBeenNthCalledWith(1, '/matchmaking/debate/status')
    expect(api.post).toHaveBeenNthCalledWith(3, '/matchmaking/roleplay/join')
    expect(api.post).toHaveBeenNthCalledWith(4, '/matchmaking/roleplay/leave')
    expect(api.get).toHaveBeenNthCalledWith(2, '/matchmaking/roleplay/status')
  })

  it('calls debate session endpoints', async () => {
    api.get.mockResolvedValue({ data: { id: 11 } })
    api.post.mockResolvedValue({ data: { id: 11 } })

    await DebateService.getSession(11)
    await DebateService.startSession(11)
    await DebateService.startRoundOne(11)
    await DebateService.startRoundTwo(11)
    await DebateService.startRoundThree(11)

    expect(api.get).toHaveBeenCalledWith('/debates/11')
    expect(api.post).toHaveBeenNthCalledWith(1, '/debates/11/start')
    expect(api.post).toHaveBeenNthCalledWith(2, '/debates/runtime/11/round1')
    expect(api.post).toHaveBeenNthCalledWith(3, '/debates/runtime/11/round2')
    expect(api.post).toHaveBeenNthCalledWith(4, '/debates/runtime/11/round3')
  })

  it('calls roleplay session endpoints', async () => {
    api.get.mockResolvedValue({ data: { id: 22 } })
    api.post.mockResolvedValue({ data: { id: 22 } })

    await RoleplayService.getSession(22)
    await RoleplayService.startRoleplay(22)

    expect(api.get).toHaveBeenCalledWith('/roleplay/22')
    expect(api.post).toHaveBeenCalledWith('/roleplay/22/start')
  })

  it('calls feedback endpoint with ratings payload', async () => {
    const payload = { sessionId: 11, sessionType: 'DEBATE', targetUserId: 2, fluencyRating: 5 }
    api.post.mockResolvedValue({ data: { message: 'Feedback submitted' } })

    await expect(FeedbackService.submitFeedback(payload)).resolves.toEqual({ message: 'Feedback submitted' })

    expect(api.post).toHaveBeenCalledWith('/feedback', payload)
  })

  it('calls GD endpoints with expected routes and payloads', async () => {
    api.get.mockResolvedValue({ data: [] })
    api.post.mockResolvedValue({ data: { message: 'ok' } })

    await GDService.getActiveRooms()
    await GDService.getRoom(33)
    await GDService.getLeaderboard(33)
    await GDService.createRoom({ topic: 'AI', maxParticipants: 8 })
    await GDService.joinRoom(33)
    await GDService.leaveRoom(33)
    await GDService.markSpoken(33)
    await GDService.giveStar({ sessionId: 33, receiverId: 2 })
    await GDService.closeRoom(33)

    expect(api.get).toHaveBeenNthCalledWith(1, '/gd/active')
    expect(api.get).toHaveBeenNthCalledWith(2, '/gd/33')
    expect(api.get).toHaveBeenNthCalledWith(3, '/gd/33/leaderboard')
    expect(api.post).toHaveBeenNthCalledWith(1, '/gd', { topic: 'AI', maxParticipants: 8 })
    expect(api.post).toHaveBeenNthCalledWith(2, '/gd/33/join')
    expect(api.post).toHaveBeenNthCalledWith(3, '/gd/33/leave')
    expect(api.post).toHaveBeenNthCalledWith(4, '/gd/33/spoken')
    expect(api.post).toHaveBeenNthCalledWith(5, '/gd/star', { sessionId: 33, receiverId: 2 })
    expect(api.post).toHaveBeenNthCalledWith(6, '/gd/33/close')
  })
})
