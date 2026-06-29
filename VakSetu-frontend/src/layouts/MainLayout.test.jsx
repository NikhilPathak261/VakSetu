import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import MainLayout from './MainLayout'

const authMocks = vi.hoisted(() => ({
  logoutWithServer: vi.fn(),
}))

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    currentUser: { id: 1, name: 'Sam' },
    logoutWithServer: authMocks.logoutWithServer,
  }),
}))

vi.mock('../components/common/EventPanel', () => ({
  default: () => <aside>Realtime panel</aside>,
}))

describe('MainLayout', () => {
  beforeEach(() => {
    authMocks.logoutWithServer.mockReset()
  })

  it('renders navigation and logs out through the server-backed auth flow', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route element={<MainLayout />}>
            <Route path="/dashboard" element={<h1>Dashboard content</h1>} />
          </Route>
          <Route path="/login" element={<h1>Login page</h1>} />
        </Routes>
      </MemoryRouter>,
    )

    expect(screen.getByText('Sam')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /dashboard/i })).toHaveAttribute('href', '/dashboard')
    expect(screen.getByText('Dashboard content')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /logout/i }))

    expect(authMocks.logoutWithServer).toHaveBeenCalledTimes(1)
    expect(screen.getByText('Login page')).toBeInTheDocument()
  })
})
