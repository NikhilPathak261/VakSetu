import api from './api'

const RoleplayService = {
  getSession(sessionId) {
    return api.get(`/roleplay/${sessionId}`).then((response) => response.data)
  },

  startRoleplay(sessionId) {
    return api.post(`/roleplay/${sessionId}/start`).then((response) => response.data)
  },
}

export default RoleplayService
