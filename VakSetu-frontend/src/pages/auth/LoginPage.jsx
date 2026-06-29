import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import Alert from '../../components/common/Alert'
import { useAuth } from '../../hooks/useAuth'
import { routes } from '../../constants/routes'

function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setLoading(true)

    try {
      await login(form)
      navigate(location.state?.from?.pathname || routes.dashboard, { replace: true })
    } catch (exception) {
      setError(exception.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <form className="form-card" onSubmit={handleSubmit}>
      <label>
        Email
        <input
          type="email"
          value={form.email}
          onChange={(event) => setForm({ ...form, email: event.target.value })}
          required
        />
      </label>
      <label>
        Password
        <input
          type="password"
          value={form.password}
          onChange={(event) => setForm({ ...form, password: event.target.value })}
          required
          minLength={8}
        />
      </label>
      <Alert variant="error">{error}</Alert>
      <button type="submit" disabled={loading}>
        {loading ? 'Signing in' : 'Sign in'}
      </button>
      <p className="muted">
        New here? <Link to={routes.register}>Create an account</Link>
      </p>
    </form>
  )
}

export default LoginPage
