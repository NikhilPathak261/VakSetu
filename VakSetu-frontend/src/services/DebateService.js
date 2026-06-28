import api from './api'

const DebateService = {
  getSession(sessionId) {
    return api.get(`/debates/${sessionId}`).then((response) => response.data)
  },

  startSession(sessionId) {
    return api.post(`/debates/${sessionId}/start`).then((response) => response.data)
  },
}

export default DebateService
