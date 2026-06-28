import api from './api'

const UserService = {
  getProfile() {
    return api.get('/users/me').then((response) => response.data)
  },

  updateProfile(payload) {
    return api.put('/users/me', payload).then((response) => response.data)
  },
}

export default UserService
