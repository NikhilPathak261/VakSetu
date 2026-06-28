import api from './api'

const GDService = {
  getActiveRooms() {
    return api.get('/gd/active').then((response) => response.data)
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
}

export default GDService
