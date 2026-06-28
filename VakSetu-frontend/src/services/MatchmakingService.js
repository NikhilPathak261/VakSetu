import api from './api'

const MatchmakingService = {
  joinDebateQueue(topicId) {
    return api.post('/matchmaking/debate/join', { topicId }).then((response) => response.data)
  },

  leaveDebateQueue() {
    return api.post('/matchmaking/debate/leave').then((response) => response.data)
  },

  getDebateStatus() {
    return api.get('/matchmaking/debate/status').then((response) => response.data)
  },

  joinRoleplayQueue() {
    return api.post('/matchmaking/roleplay/join').then((response) => response.data)
  },

  leaveRoleplayQueue() {
    return api.post('/matchmaking/roleplay/leave').then((response) => response.data)
  },

  getRoleplayStatus() {
    return api.get('/matchmaking/roleplay/status').then((response) => response.data)
  },
}

export default MatchmakingService
