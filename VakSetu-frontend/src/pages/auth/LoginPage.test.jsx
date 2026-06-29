import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import LoginPage from './LoginPage'

const authMocks = vi.hoisted(() => ({
  login: vi.fn(),
}))

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    login: authMocks.login,
  }),
}))

describe('LoginPage', () => {
  beforeEach(() => {
    authMocks.login.mockReset()
  })

  it('shows backend login errors through shared alert feedback', async () => {
    const user = userEvent.setup()
    authMocks.login.mockRejectedValue(new Error('Invalid credentials'))

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    )

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'sam@example.com' } })
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'password123' } })
    await user.click(screen.getByRole('button', { name: /sign in/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid credentials')
    expect(authMocks.login).toHaveBeenCalledWith({
      email: 'sam@example.com',
      password: 'password123',
    })
  })
})
