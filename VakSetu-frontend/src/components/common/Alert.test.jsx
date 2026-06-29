import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import Alert from './Alert'

describe('Alert', () => {
  it('renders error messages with an alert role', () => {
    render(<Alert variant="error">Something failed</Alert>)

    expect(screen.getByRole('alert')).toHaveTextContent('Something failed')
  })

  it('skips empty messages', () => {
    const { container } = render(<Alert variant="success" />)

    expect(container).toBeEmptyDOMElement()
  })
})
