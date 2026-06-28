import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import EventPanel from '../components/common/EventPanel'
import { routes } from '../constants/routes'
import { useAuth } from '../hooks/useAuth'

function MainLayout() {
  const { currentUser, logoutWithServer } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logoutWithServer()
    navigate(routes.login, { replace: true })
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div>
          <p className="brand">VakSetu</p>
          <p className="muted">{currentUser?.name || 'Learner'}</p>
        </div>
        <nav>
          <NavLink to={routes.dashboard}>Dashboard</NavLink>
          <NavLink to={routes.profile}>Profile</NavLink>
          <NavLink to={routes.debate}>Debate</NavLink>
          <NavLink to={routes.gd}>GD</NavLink>
          <NavLink to={routes.roleplay}>Roleplay</NavLink>
        </nav>
        <button type="button" className="ghost-button" onClick={handleLogout}>
          Logout
        </button>
      </aside>
      <main className="content">
        <Outlet />
      </main>
      <EventPanel />
    </div>
  )
}

export default MainLayout
