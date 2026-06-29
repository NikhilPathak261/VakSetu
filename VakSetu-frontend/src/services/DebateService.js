import api from './api'

const DebateService = {
  getSession(sessionId) {
    return api.get(`/debates/${sessionId}`).then((response) => response.data)
  },

  startSession(sessionId) {
    return api.post(`/debates/${sessionId}/start`).then((response) => response.data)
  },

  startRoundOne(sessionId) {
    return api.post(`/debates/runtime/${sessionId}/round1`).then((response) => response.data)
  },

  startRoundTwo(sessionId) {
    return api.post(`/debates/runtime/${sessionId}/round2`).then((response) => response.data)
  },

  startRoundThree(sessionId) {
    return api.post(`/debates/runtime/${sessionId}/round3`).then((response) => response.data)
  },
}

export default DebateService
