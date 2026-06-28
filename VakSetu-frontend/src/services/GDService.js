import api from './api'

const GDService = {
  getActiveRooms() {
    return api.get('/gd/active').then((response) => response.data)
  },

  getRoom(sessionId) {
    return api.get(`/gd/${sessionId}`).then((response) => response.data)
  },

  getLeaderboard(sessionId) {
    return api.get(`/gd/${sessionId}/leaderboard`).then((response) => response.data)
  },

  createRoom(payload) {
    return api.post('/gd', payload).then((response) => response.data)
  },

  joinRoom(sessionId) {
    return api.post(`/gd/${sessionId}/join`).then((response) => response.data)
  },

  leaveRoom(sessionId) {
    return api.post(`/gd/${sessionId}/leave`).then((response) => response.data)
  },

  markSpoken(sessionId) {
    return api.post(`/gd/${sessionId}/spoken`).then((response) => response.data)
  },

  giveStar(payload) {
    return api.post('/gd/star', payload).then((response) => response.data)
  },

  closeRoom(sessionId) {
    return api.post(`/gd/${sessionId}/close`).then((response) => response.data)
  },
}

export default GDService
