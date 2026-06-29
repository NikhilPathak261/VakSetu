import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ProtectedRoute from './ProtectedRoute'

const authMocks = vi.hoisted(() => ({
  authLoading: false,
  isAuthenticated: false,
}))

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    authLoading: authMocks.authLoading,
    isAuthenticated: authMocks.isAuthenticated,
  }),
}))

function renderProtectedRoute() {
  return render(
    <MemoryRouter initialEntries={['/dashboard']}>
      <Routes>
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<h1>Private dashboard</h1>} />
        </Route>
        <Route path="/login" element={<h1>Login page</h1>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    authMocks.authLoading = false
    authMocks.isAuthenticated = false
  })

  it('redirects unauthenticated users to login', () => {
    renderProtectedRoute()

    expect(screen.getByText('Login page')).toBeInTheDocument()
  })

  it('renders protected content after authentication', () => {
    authMocks.isAuthenticated = true

    renderProtectedRoute()

    expect(screen.getByText('Private dashboard')).toBeInTheDocument()
  })
})
