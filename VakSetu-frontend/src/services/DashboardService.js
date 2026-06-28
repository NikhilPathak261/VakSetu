import api from './api'

const DashboardService = {
  getSummary() {
    return api.get('/dashboard/me').then((response) => response.data)
  },

  getSkillHistory() {
    return api.get('/dashboard/me/skill-history').then((response) => response.data)
  },

  getReputationHistory() {
    return api.get('/dashboard/me/reputation-history').then((response) => response.data)
  },
}

export default DashboardService
