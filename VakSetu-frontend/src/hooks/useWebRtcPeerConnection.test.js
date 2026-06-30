import { act, renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { websocketEvents, webRtcSignalTypes } from '../constants/websocket'
import { useWebRtcPeerConnection } from './useWebRtcPeerConnection'

const authMocks = vi.hoisted(() => ({
  currentUser: { id: 1, name: 'Sam' },
}))

const websocketMocks = vi.hoisted(() => ({
  connected: true,
  publish: vi.fn(),
  subscribe: vi.fn(),
  subscriptionHandler: null,
  unsubscribe: vi.fn(),
}))

vi.mock('./useAuth', () => ({
  useAuth: () => ({
    currentUser: authMocks.currentUser,
  }),
}))

vi.mock('./useWebSocketEvents', () => ({
  useWebSocketEvents: () => ({
    connected: websocketMocks.connected,
    publish: websocketMocks.publish,
    subscribe: websocketMocks.subscribe,
  }),
}))

function createTrack(kind) {
  return {
    enabled: true,
    kind,
    stop: vi.fn(),
  }
}

function createStream() {
  const audioTrack = createTrack('audio')
  const videoTrack = createTrack('video')
  const tracks = [audioTrack, videoTrack]

  return {
    addTrack: vi.fn(),
    audioTrack,
    getAudioTracks: vi.fn(() => [audioTrack]),
    getTracks: vi.fn(() => tracks),
    getVideoTracks: vi.fn(() => [videoTrack]),
    videoTrack,
  }
}

function createPeerConnectionMock() {
  return {
    addIceCandidate: vi.fn(() => Promise.resolve()),
    addTrack: vi.fn(),
    close: vi.fn(),
    connectionState: 'new',
    createAnswer: vi.fn(() => Promise.resolve({ sdp: 'answer-sdp', type: 'answer' })),
    createOffer: vi.fn(() => Promise.resolve({ sdp: 'offer-sdp', type: 'offer' })),
    onconnectionstatechange: null,
    onicecandidate: null,
    ontrack: null,
    setLocalDescription: vi.fn(() => Promise.resolve()),
    setRemoteDescription: vi.fn(() => Promise.resolve()),
  }
}

describe('useWebRtcPeerConnection', () => {
  let localStream
  let peerConnection

  beforeEach(() => {
    authMocks.currentUser = { id: 1, name: 'Sam' }
    websocketMocks.connected = true
    websocketMocks.publish.mockReset()
    websocketMocks.subscribe.mockReset()
    websocketMocks.subscriptionHandler = null
    websocketMocks.unsubscribe.mockReset()
    websocketMocks.subscribe.mockImplementation((topic, handler) => {
      websocketMocks.subscriptionHandler = handler
      return websocketMocks.unsubscribe
    })

    localStream = createStream()
    peerConnection = createPeerConnectionMock()

    globalThis.MediaStream = vi.fn(function MediaStream() {
      return createStream()
    })
    globalThis.RTCPeerConnection = vi.fn(function RTCPeerConnection() {
      return peerConnection
    })
    globalThis.RTCSessionDescription = vi.fn(function RTCSessionDescription(description) {
      return { ...description, wrapped: 'session' }
    })
    globalThis.RTCIceCandidate = vi.fn(function RTCIceCandidate(candidate) {
      return { ...candidate, wrapped: 'ice' }
    })

    Object.defineProperty(globalThis.navigator, 'mediaDevices', {
      configurable: true,
      value: {
        getUserMedia: vi.fn(() => Promise.resolve(localStream)),
      },
    })
  })

  function renderPeerHook(props = {}) {
    return renderHook(() => useWebRtcPeerConnection({
      partner: { id: 2, name: 'Priya' },
      sessionId: '55',
      sessionType: 'DEBATE',
      ...props,
    }))
  }

  it('subscribes to the session signaling topic when realtime is connected', () => {
    renderPeerHook()

    expect(websocketMocks.subscribe).toHaveBeenCalledWith('/topic/webrtc/55', expect.any(Function))
  })

  it('starts local media, attaches tracks, and moves into ready state', async () => {
    const { result } = renderPeerHook()

    await act(async () => {
      await result.current.startLocalMedia()
    })

    expect(navigator.mediaDevices.getUserMedia).toHaveBeenCalledWith({ audio: true, video: true })
    expect(globalThis.RTCPeerConnection).toHaveBeenCalledWith({
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],
    })
    expect(peerConnection.addTrack).toHaveBeenCalledWith(localStream.audioTrack, localStream)
    expect(peerConnection.addTrack).toHaveBeenCalledWith(localStream.videoTrack, localStream)
    expect(result.current.callState).toBe('ready')
  })

  it('starts an outgoing call and publishes an offer', async () => {
    const { result } = renderPeerHook()

    await act(async () => {
      await result.current.startCall()
    })

    expect(peerConnection.createOffer).toHaveBeenCalledTimes(1)
    expect(peerConnection.setLocalDescription).toHaveBeenCalledWith({ sdp: 'offer-sdp', type: 'offer' })
    expect(websocketMocks.publish).toHaveBeenCalledWith('/app/webrtc/signal', {
      sessionId: 55,
      sessionType: 'DEBATE',
      receiverUserId: 2,
      signalType: webRtcSignalTypes.offer,
      signalData: { sdp: 'offer-sdp', type: 'offer' },
    })
  })

  it('answers incoming offers and ignores self-sent signals', async () => {
    renderPeerHook()

    await act(async () => {
      await websocketMocks.subscriptionHandler({
        eventType: websocketEvents.webRtcOffer,
        payload: {
          receiverUserId: 1,
          senderUserId: 1,
          signalData: { sdp: 'ignored', type: 'offer' },
        },
      })
    })

    expect(peerConnection.setRemoteDescription).not.toHaveBeenCalled()

    await act(async () => {
      await websocketMocks.subscriptionHandler({
        eventType: websocketEvents.webRtcOffer,
        payload: {
          receiverUserId: 1,
          senderUserId: 2,
          signalData: { sdp: 'remote-offer', type: 'offer' },
        },
      })
    })

    expect(globalThis.RTCSessionDescription).toHaveBeenCalledWith({ sdp: 'remote-offer', type: 'offer' })
    expect(peerConnection.setRemoteDescription).toHaveBeenCalledWith({
      sdp: 'remote-offer',
      type: 'offer',
      wrapped: 'session',
    })
    expect(peerConnection.createAnswer).toHaveBeenCalledTimes(1)
    expect(websocketMocks.publish).toHaveBeenLastCalledWith('/app/webrtc/signal', {
      sessionId: 55,
      sessionType: 'DEBATE',
      receiverUserId: 2,
      signalType: webRtcSignalTypes.answer,
      signalData: { sdp: 'answer-sdp', type: 'answer' },
    })
  })

  it('handles incoming answers and ICE candidates', async () => {
    const { result } = renderPeerHook()

    await act(async () => {
      await websocketMocks.subscriptionHandler({
        eventType: websocketEvents.webRtcAnswer,
        payload: {
          receiverUserId: 1,
          senderUserId: 2,
          signalData: { sdp: 'remote-answer', type: 'answer' },
        },
      })
    })

    expect(peerConnection.setRemoteDescription).toHaveBeenCalledWith({
      sdp: 'remote-answer',
      type: 'answer',
      wrapped: 'session',
    })
    expect(result.current.callState).toBe('connecting')

    await act(async () => {
      await websocketMocks.subscriptionHandler({
        eventType: websocketEvents.iceCandidate,
        payload: {
          receiverUserId: 1,
          senderUserId: 2,
          signalData: { candidate: 'candidate', sdpMLineIndex: 0, sdpMid: '0' },
        },
      })
    })

    expect(globalThis.RTCIceCandidate).toHaveBeenCalledWith({ candidate: 'candidate', sdpMLineIndex: 0, sdpMid: '0' })
    expect(peerConnection.addIceCandidate).toHaveBeenCalledWith({
      candidate: 'candidate',
      sdpMLineIndex: 0,
      sdpMid: '0',
      wrapped: 'ice',
    })
  })

  it('publishes local ICE candidates and reacts to connection state changes', async () => {
    const { result } = renderPeerHook()

    await act(async () => {
      await result.current.startLocalMedia()
    })

    act(() => {
      peerConnection.onicecandidate({
        candidate: {
          toJSON: () => ({ candidate: 'local-candidate' }),
        },
      })
    })

    expect(websocketMocks.publish).toHaveBeenCalledWith('/app/webrtc/signal', {
      sessionId: 55,
      sessionType: 'DEBATE',
      receiverUserId: 2,
      signalType: webRtcSignalTypes.iceCandidate,
      signalData: { candidate: 'local-candidate' },
    })

    act(() => {
      peerConnection.connectionState = 'connected'
      peerConnection.onconnectionstatechange()
    })
    expect(result.current.callState).toBe('connected')

    act(() => {
      peerConnection.connectionState = 'failed'
      peerConnection.onconnectionstatechange()
    })
    expect(result.current.callState).toBe('ended')
  })

  it('toggles local tracks and cleans up media on hang up', async () => {
    const { result } = renderPeerHook()

    await act(async () => {
      await result.current.startLocalMedia()
    })

    act(() => {
      result.current.toggleAudio()
      result.current.toggleVideo()
    })

    expect(localStream.audioTrack.enabled).toBe(false)
    expect(localStream.videoTrack.enabled).toBe(false)
    expect(result.current.audioEnabled).toBe(false)
    expect(result.current.videoEnabled).toBe(false)

    act(() => {
      result.current.hangUp()
    })

    expect(peerConnection.close).toHaveBeenCalledTimes(1)
    expect(localStream.audioTrack.stop).toHaveBeenCalledTimes(1)
    expect(localStream.videoTrack.stop).toHaveBeenCalledTimes(1)
    expect(result.current.callState).toBe('idle')
    expect(result.current.audioEnabled).toBe(true)
    expect(result.current.videoEnabled).toBe(true)
  })

  it('surfaces media errors without leaving the call in progress', async () => {
    navigator.mediaDevices.getUserMedia.mockRejectedValue(new Error('Camera blocked'))
    const { result } = renderPeerHook()

    await act(async () => {
      await result.current.startCall()
    })

    await waitFor(() => {
      expect(result.current.error).toBe('Camera blocked')
    })
    expect(result.current.callState).toBe('ready')
  })
})
