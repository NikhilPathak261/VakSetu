import { createRef } from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import WebRtcCallPanel from './WebRtcCallPanel'

const peerMocks = vi.hoisted(() => ({
  audioEnabled: true,
  callState: 'idle',
  connected: true,
  error: '',
  hangUp: vi.fn(),
  localVideoRef: { current: null },
  remoteVideoRef: { current: null },
  startCall: vi.fn(),
  startLocalMedia: vi.fn(),
  toggleAudio: vi.fn(),
  toggleVideo: vi.fn(),
  videoEnabled: true,
}))

vi.mock('../../hooks/useWebRtcPeerConnection', () => ({
  useWebRtcPeerConnection: () => peerMocks,
}))

describe('WebRtcCallPanel', () => {
  beforeEach(() => {
    peerMocks.audioEnabled = true
    peerMocks.callState = 'idle'
    peerMocks.connected = true
    peerMocks.error = ''
    peerMocks.hangUp.mockReset()
    peerMocks.localVideoRef = createRef()
    peerMocks.remoteVideoRef = createRef()
    peerMocks.startCall.mockReset()
    peerMocks.startLocalMedia.mockReset()
    peerMocks.toggleAudio.mockReset()
    peerMocks.toggleVideo.mockReset()
    peerMocks.videoEnabled = true
  })

  it('starts local media and a partner call while realtime is ready', async () => {
    const user = userEvent.setup()

    render(<WebRtcCallPanel sessionId="5" sessionType="DEBATE" partner={{ id: 2, name: 'Priya' }} />)

    expect(screen.getByText('Call with Priya')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /start media/i }))
    await user.click(screen.getByRole('button', { name: /start call/i }))

    expect(peerMocks.startLocalMedia).toHaveBeenCalledTimes(1)
    expect(peerMocks.startCall).toHaveBeenCalledTimes(1)
  })

  it('disables call start without realtime readiness and shows hook errors', () => {
    peerMocks.connected = false
    peerMocks.error = 'Camera permission denied'

    render(<WebRtcCallPanel sessionId="5" sessionType="ROLEPLAY" partner={{ id: 2, name: 'Priya' }} />)

    expect(screen.getByRole('button', { name: /start media/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /start call/i })).toBeDisabled()
    expect(screen.getByRole('alert')).toHaveTextContent('Camera permission denied')
  })
})
