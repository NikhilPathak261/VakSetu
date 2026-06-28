import { useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import UserService from '../../services/UserService'

function ProfilePage() {
  const { currentUser } = useAuth()
  const [name, setName] = useState(currentUser?.name || '')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    setError('')

    try {
      await UserService.updateProfile({ name })
      setMessage('Profile updated')
    } catch (exception) {
      setError(exception.message)
    }
  }

  return (
    <section className="page-stack">
      <header>
        <p className="eyebrow">Profile</p>
        <h1>Account details</h1>
      </header>
      <form className="form-card compact" onSubmit={handleSubmit}>
        <label>
          Name
          <input type="text" value={name} onChange={(event) => setName(event.target.value)} required />
        </label>
        <label>
          Email
          <input type="email" value={currentUser?.email || ''} disabled />
        </label>
        {message && <p className="success-text">{message}</p>}
        {error && <p className="error-text">{error}</p>}
        <button type="submit">Save</button>
      </form>
    </section>
  )
}

export default ProfilePage
