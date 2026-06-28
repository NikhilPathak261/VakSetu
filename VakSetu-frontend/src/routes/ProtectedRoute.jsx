import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { routes } from '../constants/routes'

function ProtectedRoute() {
  const { authLoading, isAuthenticated } = useAuth()
  const location = useLocation()

  if (authLoading) {
    return <main className="centered-page">Loading</main>
  }

  if (!isAuthenticated) {
    return <Navigate to={routes.login} state={{ from: location }} replace />
  }

  return <Outlet />
}

export default ProtectedRoute
