import { Navigate, Route, Routes } from 'react-router-dom'
import AuthLayout from './layouts/AuthLayout.jsx'
import MainLayout from './layouts/MainLayout.jsx'
import LoginPage from './pages/auth/LoginPage.jsx'
import RegisterPage from './pages/auth/RegisterPage.jsx'
import DashboardPage from './pages/dashboard/DashboardPage.jsx'
import ProfilePage from './pages/profile/ProfilePage.jsx'
import DebateLobbyPage from './pages/debate/DebateLobbyPage.jsx'
import DebateSessionPage from './pages/debate/DebateSessionPage.jsx'
import GDLobbyPage from './pages/gd/GDLobbyPage.jsx'
import GDRoomPage from './pages/gd/GDRoomPage.jsx'
import RoleplayLobbyPage from './pages/roleplay/RoleplayLobbyPage.jsx'
import RoleplaySessionPage from './pages/roleplay/RoleplaySessionPage.jsx'
import ProtectedRoute from './routes/ProtectedRoute.jsx'
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>
      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/debate" element={<DebateLobbyPage />} />
          <Route path="/debate/session/:sessionId" element={<DebateSessionPage />} />
          <Route path="/gd" element={<GDLobbyPage />} />
          <Route path="/gd/room/:sessionId" element={<GDRoomPage />} />
          <Route path="/roleplay" element={<RoleplayLobbyPage />} />
          <Route path="/roleplay/session/:sessionId" element={<RoleplaySessionPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}

export default App
