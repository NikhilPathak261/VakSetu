import { useCallback, useEffect, useRef, useState } from 'react'
import { websocketEvents, websocketTopics, webRtcSignalTypes } from '../constants/websocket'
import { useAuth } from './useAuth'
import { useWebSocketEvents } from './useWebSocketEvents'

const PEER_CONFIG = {
  iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],
}

export function useWebRtcPeerConnection({ sessionId, sessionType, partner }) {
  const { currentUser } = useAuth()
  const webSocket = useWebSocketEvents()
  const connected = webSocket?.connected ?? false
  const publish = webSocket?.publish
  const subscribe = webSocket?.subscribe
  const peerConnectionRef = useRef(null)
  const localStreamRef = useRef(null)
  const remoteStreamRef = useRef(new MediaStream())
  const localVideoRef = useRef(null)
  const remoteVideoRef = useRef(null)
  const [callState, setCallState] = useState('idle')
  const [error, setError] = useState('')
  const [audioEnabled, setAudioEnabled] = useState(true)
  const [videoEnabled, setVideoEnabled] = useState(true)

  const sendSignal = useCallback(
    (signalType, signalData) => {
      if (!publish) {
        throw new Error('Realtime connection is not ready')
      }

      publish('/app/webrtc/signal', {
        sessionId: Number(sessionId),
        sessionType,
        receiverUserId: partner?.id,
        signalType,
        signalData,
      })
    },
    [partner?.id, publish, sessionId, sessionType],
  )

  const attachLocalStream = useCallback((stream) => {
    localStreamRef.current = stream

    if (localVideoRef.current) {
      localVideoRef.current.srcObject = stream
    }
  }, [])

  const attachRemoteStream = useCallback(() => {
    if (remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = remoteStreamRef.current
    }
  }, [])

  const createPeerConnection = useCallback(() => {
    if (peerConnectionRef.current) {
      return peerConnectionRef.current
    }

    const peerConnection = new RTCPeerConnection(PEER_CONFIG)

    peerConnection.onicecandidate = (event) => {
      if (event.candidate) {
        sendSignal(webRtcSignalTypes.iceCandidate, event.candidate.toJSON())
      }
    }

    peerConnection.ontrack = (event) => {
      event.streams[0]?.getTracks().forEach((track) => {
        remoteStreamRef.current.addTrack(track)
      })
      attachRemoteStream()
    }

    peerConnection.onconnectionstatechange = () => {
      if (peerConnection.connectionState === 'connected') {
        setCallState('connected')
      }

      if (['disconnected', 'failed', 'closed'].includes(peerConnection.connectionState)) {
        setCallState((currentState) => (currentState === 'idle' ? 'idle' : 'ended'))
      }
    }

    localStreamRef.current?.getTracks().forEach((track) => {
      peerConnection.addTrack(track, localStreamRef.current)
    })

    peerConnectionRef.current = peerConnection
    return peerConnection
  }, [attachRemoteStream, sendSignal])

  const startLocalMedia = useCallback(async () => {
    setError('')

    try {
      if (!localStreamRef.current) {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true, video: true })
        attachLocalStream(stream)
      }

      createPeerConnection()
      setCallState((currentState) => (currentState === 'idle' ? 'ready' : currentState))
      return localStreamRef.current
    } catch (exception) {
      setError(exception.message)
      throw exception
    }
  }, [attachLocalStream, createPeerConnection])

  const startCall = useCallback(async () => {
    setError('')
    setCallState('calling')

    try {
      await startLocalMedia()
      const peerConnection = createPeerConnection()
      const offer = await peerConnection.createOffer()
      await peerConnection.setLocalDescription(offer)
      sendSignal(webRtcSignalTypes.offer, offer)
    } catch (exception) {
      setError(exception.message)
      setCallState('ready')
    }
  }, [createPeerConnection, sendSignal, startLocalMedia])

  const handleIncomingSignal = useCallback(
    async (event) => {
      const payload = event.payload

      if (!payload || payload.senderUserId === currentUser?.id) {
        return
      }

      if (payload.receiverUserId && payload.receiverUserId !== currentUser?.id) {
        return
      }

      setError('')

      try {
        await startLocalMedia()
        const peerConnection = createPeerConnection()

        if (event.eventType === websocketEvents.webRtcOffer) {
          await peerConnection.setRemoteDescription(new RTCSessionDescription(payload.signalData))
          const answer = await peerConnection.createAnswer()
          await peerConnection.setLocalDescription(answer)
          sendSignal(webRtcSignalTypes.answer, answer)
          setCallState('answering')
          return
        }

        if (event.eventType === websocketEvents.webRtcAnswer) {
          await peerConnection.setRemoteDescription(new RTCSessionDescription(payload.signalData))
          setCallState('connecting')
          return
        }

        if (event.eventType === websocketEvents.iceCandidate && payload.signalData) {
          await peerConnection.addIceCandidate(new RTCIceCandidate(payload.signalData))
        }
      } catch (exception) {
        setError(exception.message)
      }
    },
    [createPeerConnection, currentUser?.id, sendSignal, startLocalMedia],
  )

  const toggleAudio = useCallback(() => {
    localStreamRef.current?.getAudioTracks().forEach((track) => {
      track.enabled = !track.enabled
      setAudioEnabled(track.enabled)
    })
  }, [])

  const toggleVideo = useCallback(() => {
    localStreamRef.current?.getVideoTracks().forEach((track) => {
      track.enabled = !track.enabled
      setVideoEnabled(track.enabled)
    })
  }, [])

  const hangUp = useCallback(() => {
    peerConnectionRef.current?.close()
    peerConnectionRef.current = null

    localStreamRef.current?.getTracks().forEach((track) => track.stop())
    localStreamRef.current = null
    remoteStreamRef.current = new MediaStream()

    if (localVideoRef.current) {
      localVideoRef.current.srcObject = null
    }

    if (remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = null
    }

    setAudioEnabled(true)
    setVideoEnabled(true)
    setCallState('idle')
  }, [])

  useEffect(() => {
    if (!connected || !sessionId || !subscribe) {
      return undefined
    }

    return subscribe(websocketTopics.webRtcSession(sessionId), handleIncomingSignal)
  }, [connected, handleIncomingSignal, sessionId, subscribe])

  useEffect(() => () => {
    hangUp()
  }, [hangUp])

  useEffect(() => {
    if (localVideoRef.current && localStreamRef.current) {
      localVideoRef.current.srcObject = localStreamRef.current
    }

    attachRemoteStream()
  }, [attachRemoteStream])

  return {
    audioEnabled,
    callState,
    connected,
    error,
    hangUp,
    localVideoRef,
    remoteVideoRef,
    startCall,
    startLocalMedia,
    toggleAudio,
    toggleVideo,
    videoEnabled,
  }
}
