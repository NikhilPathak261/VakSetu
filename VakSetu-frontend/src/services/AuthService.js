import api from './api'

const AuthService = {
  register(payload) {
    return api.post('/auth/register', payload).then((response) => response.data)
  },

  login(payload) {
    return api.post('/auth/login', payload).then((response) => response.data)
  },

  refresh(refreshToken) {
    return api.post('/auth/refresh', { refreshToken }).then((response) => response.data)
  },

  logout(refreshToken) {
    return api.post('/auth/logout', { refreshToken }).then((response) => response.data)
  },
}

export default AuthService
