import { beforeEach, describe, expect, it, vi } from 'vitest'

function createAxiosMock() {
  const handlers = {
    request: null,
    responseSuccess: null,
    responseError: null,
  }

  const instance = {
    interceptors: {
      request: {
        use: vi.fn((handler) => {
          handlers.request = handler
        }),
      },
      response: {
        use: vi.fn((successHandler, errorHandler) => {
          handlers.responseSuccess = successHandler
          handlers.responseError = errorHandler
        }),
      },
    },
  }

  const axios = {
    create: vi.fn(() => instance),
  }

  return { axios, handlers, instance }
}

describe('api client', () => {
  beforeEach(() => {
    vi.resetModules()
    localStorage.clear()
  })

  it('configures the base API client and attaches stored access tokens', async () => {
    const { axios, handlers } = createAxiosMock()
    vi.doMock('axios', () => ({ default: axios }))

    await import('./api')
    localStorage.setItem('vaksetu_access_token', 'access-token')

    const config = handlers.request({ headers: {} })

    expect(axios.create).toHaveBeenCalledWith({
      baseURL: 'http://localhost:8080',
      headers: {
        'Content-Type': 'application/json',
      },
    })
    expect(config.headers.Authorization).toBe('Bearer access-token')
  })

  it('leaves Authorization empty when no access token exists', async () => {
    const { axios, handlers } = createAxiosMock()
    vi.doMock('axios', () => ({ default: axios }))

    await import('./api')

    const config = handlers.request({ headers: {} })

    expect(config.headers.Authorization).toBeUndefined()
  })

  it('normalizes backend error messages into Error instances', async () => {
    const { axios, handlers } = createAxiosMock()
    vi.doMock('axios', () => ({ default: axios }))

    await import('./api')

    await expect(
      handlers.responseError({
        response: {
          data: {
            message: 'Invalid request',
          },
        },
      }),
    ).rejects.toThrow('Invalid request')
  })

  it('uses a generic error message when backend response has no message', async () => {
    const { axios, handlers } = createAxiosMock()
    vi.doMock('axios', () => ({ default: axios }))

    await import('./api')

    await expect(handlers.responseError({ response: { data: {} } })).rejects.toThrow('Something went wrong')
  })
})
