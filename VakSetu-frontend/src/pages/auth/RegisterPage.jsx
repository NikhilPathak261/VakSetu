import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Alert from '../../components/common/Alert'
import { useAuth } from '../../hooks/useAuth'
import { routes } from '../../constants/routes'

function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setLoading(true)

    try {
      await register(form)
      navigate(routes.dashboard, { replace: true })
    } catch (exception) {
      setError(exception.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <form className="form-card" onSubmit={handleSubmit}>
      <label>
        Name
        <input
          type="text"
          value={form.name}
          onChange={(event) => setForm({ ...form, name: event.target.value })}
          required
        />
      </label>
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
        {loading ? 'Creating account' : 'Create account'}
      </button>
      <p className="muted">
        Already registered? <Link to={routes.login}>Sign in</Link>
      </p>
    </form>
  )
}

export default RegisterPage
