import { Outlet } from 'react-router-dom'

function AuthLayout() {
  return (
    <main className="auth-layout">
      <section className="auth-panel">
        <div>
          <p className="eyebrow">VakSetu</p>
          <h1>Practice better conversations</h1>
        </div>
        <Outlet />
      </section>
    </main>
  )
}

export default AuthLayout
