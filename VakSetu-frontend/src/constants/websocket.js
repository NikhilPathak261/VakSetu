export const websocketTopics = {
  match: '/topic/match',
  gd: '/topic/gd',
  debate: '/topic/debate',
  roleplay: '/topic/roleplay',
  system: '/topic/system',
  webRtcSession: (sessionId) => `/topic/webrtc/${sessionId}`,
}

export const websocketEvents = {
  matchFound: 'MATCH_FOUND',
  userJoined: 'USER_JOINED',
  userLeft: 'USER_LEFT',
  starReceived: 'STAR_RECEIVED',
  leaderboardUpdated: 'LEADERBOARD_UPDATED',
  gdRoomCreated: 'GD_ROOM_CREATED',
  gdRoomClosed: 'GD_ROOM_CLOSED',
  webRtcOffer: 'WEBRTC_OFFER',
  webRtcAnswer: 'WEBRTC_ANSWER',
  iceCandidate: 'ICE_CANDIDATE',
}

export const webRtcSignalTypes = {
  offer: 'OFFER',
  answer: 'ANSWER',
  iceCandidate: 'ICE_CANDIDATE',
}
