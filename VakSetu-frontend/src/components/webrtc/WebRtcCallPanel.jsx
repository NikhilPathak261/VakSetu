import { useWebRtcPeerConnection } from '../../hooks/useWebRtcPeerConnection'

function WebRtcCallPanel({ sessionId, sessionType, partner }) {
  const {
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
  } = useWebRtcPeerConnection({ sessionId, sessionType, partner })

  return (
    <section className="form-card webrtc-panel">
      <header>
        <p className="eyebrow">Live Practice</p>
        <h2>{partner ? `Call with ${partner.name}` : 'Waiting for session partner'}</h2>
        <p className="muted">
          {connected ? `Realtime ready - ${callState}` : 'Realtime connection is still starting'}
        </p>
      </header>
      <div className="video-grid">
        <article>
          <video ref={localVideoRef} autoPlay muted playsInline />
          <span>You</span>
        </article>
        <article>
          <video ref={remoteVideoRef} autoPlay playsInline />
          <span>{partner?.name || 'Partner'}</span>
        </article>
      </div>
      <div className="toolbar">
        <button type="button" onClick={startLocalMedia} disabled={!connected || callState !== 'idle'}>
          Start media
        </button>
        <button type="button" onClick={startCall} disabled={!connected || !partner || callState === 'calling'}>
          Start call
        </button>
        <button type="button" className="ghost-button" onClick={toggleAudio} disabled={callState === 'idle'}>
          {audioEnabled ? 'Mute' : 'Unmute'}
        </button>
        <button type="button" className="ghost-button" onClick={toggleVideo} disabled={callState === 'idle'}>
          {videoEnabled ? 'Camera off' : 'Camera on'}
        </button>
        <button type="button" className="ghost-button" onClick={hangUp} disabled={callState === 'idle'}>
          Hang up
        </button>
      </div>
      {error && <p className="error-text">{error}</p>}
    </section>
  )
}

export default WebRtcCallPanel
