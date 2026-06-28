import { useState } from 'react'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import { useAuth } from '../../hooks/useAuth'
import UserService from '../../services/UserService'

function ProfilePage() {
  const { authLoading, currentUser, refreshProfile } = useAuth()
  const [name, setName] = useState(currentUser?.name || '')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    setError('')
    setSaving(true)

    try {
      await UserService.updateProfile({ name: name || currentUser.name })
      await refreshProfile()
      setMessage('Profile updated')
    } catch (exception) {
      setError(exception.message)
    } finally {
      setSaving(false)
    }
  }

  if (authLoading) {
    return <LoadingBlock label="Loading profile" />
  }

  if (!currentUser) {
    return <EmptyState title="Profile unavailable" message="Sign in again to view account details." />
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
          <input
            type="text"
            value={name || currentUser.name}
            onChange={(event) => setName(event.target.value)}
            required
          />
        </label>
        <label>
          Email
          <input type="email" value={currentUser?.email || ''} disabled />
        </label>
        {message && <p className="success-text">{message}</p>}
        {error && <p className="error-text">{error}</p>}
        <button type="submit" disabled={saving}>
          {saving ? 'Saving' : 'Save'}
        </button>
      </form>
    </section>
  )
}

export default ProfilePage
