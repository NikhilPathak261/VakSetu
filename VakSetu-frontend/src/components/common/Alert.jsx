function Alert({ children, title, variant = 'info' }) {
  if (!children) {
    return null
  }

  const role = variant === 'error' ? 'alert' : 'status'

  return (
    <div className={`alert alert-${variant}`} role={role}>
      {title && <strong>{title}</strong>}
      <span>{children}</span>
    </div>
  )
}

export default Alert
