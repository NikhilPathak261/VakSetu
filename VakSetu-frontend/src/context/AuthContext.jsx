import { useCallback, useEffect, useMemo, useState } from 'react'
import AuthContext from './AuthContextValue'
import AuthService from '../services/AuthService'
import UserService from '../services/UserService'

const ACCESS_TOKEN_KEY = 'vaksetu_access_token'
const REFRESH_TOKEN_KEY = 'vaksetu_refresh_token'

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(() => localStorage.getItem(ACCESS_TOKEN_KEY))
  const [currentUser, setCurrentUser] = useState(null)
  const [authLoading, setAuthLoading] = useState(Boolean(accessToken))

  const storeAuth = useCallback((response) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken)
    setAccessToken(response.accessToken)
  }, [])

  const login = useCallback(async (payload) => {
    const response = await AuthService.login(payload)
    storeAuth(response)
    return response
  }, [storeAuth])

  const register = useCallback(async (payload) => {
    const response = await AuthService.register(payload)
    storeAuth(response)
    return response
  }, [storeAuth])

  const logout = useCallback(() => {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    setAccessToken(null)
    setCurrentUser(null)
    setAuthLoading(false)
  }, [])

  const logoutWithServer = useCallback(async () => {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)

    try {
      if (refreshToken) {
        await AuthService.logout(refreshToken)
      }
    } finally {
      logout()
    }
  }, [logout])

  const refreshProfile = useCallback(async () => {
    const profile = await UserService.getProfile()
    setCurrentUser(profile)
    return profile
  }, [])

  useEffect(() => {
    let active = true

    if (!accessToken) {
      return undefined
    }

    UserService.getProfile()
      .then((profile) => {
        if (active) {
          setCurrentUser(profile)
        }
      })
      .catch(() => {
        if (active) {
          logout()
        }
      })
      .finally(() => {
        if (active) {
          setAuthLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [accessToken, logout])

  const value = useMemo(
    () => ({
      accessToken,
      currentUser,
      authLoading,
      isAuthenticated: Boolean(accessToken),
      login,
      register,
      logout,
      logoutWithServer,
      refreshProfile,
    }),
    [accessToken, currentUser, authLoading, login, register, logout, logoutWithServer, refreshProfile],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
