package com.vaksetu.websocket.service;

import com.vaksetu.common.enums.SessionType;
import com.vaksetu.debate.entity.DebateSession;
import com.vaksetu.debate.repository.DebateSessionRepository;
import com.vaksetu.exception.BadRequestException;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.roleplay.entity.RoleplaySession;
import com.vaksetu.roleplay.repository.RoleplaySessionRepository;
import com.vaksetu.websocket.dto.WebRtcSignalPayload;
import com.vaksetu.websocket.dto.WebRtcSignalRequest;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebRtcSignalingService {

    private static final String OFFER = "OFFER";
    private static final String ANSWER = "ANSWER";
    private static final String ICE_CANDIDATE = "ICE_CANDIDATE";
    private static final Set<String> ALLOWED_SIGNAL_TYPES = Set.of(OFFER, ANSWER, ICE_CANDIDATE);

    private final DebateSessionRepository debateSessionRepository;
    private final RoleplaySessionRepository roleplaySessionRepository;
    private final EventPublisherService eventPublisherService;

    public void forwardSignal(
            Long senderUserId,
            WebRtcSignalRequest request
    ) {
        String signalType = normalizeSignalType(request.getSignalType());
        validateParticipantAccess(senderUserId, request);

        WebRtcSignalPayload payload = WebRtcSignalPayload.builder()
                .senderUserId(senderUserId)
                .receiverUserId(request.getReceiverUserId())
                .sessionId(request.getSessionId())
                .sessionType(request.getSessionType())
                .signalType(signalType)
                .signalData(request.getSignalData())
                .build();

        eventPublisherService.publishWebRtcSignal(
                toEventType(signalType),
                request.getSessionId(),
                senderUserId,
                payload
        );
    }

    private String normalizeSignalType(String signalType) {
        String normalizedSignalType = signalType.trim().toUpperCase();

        if (!ALLOWED_SIGNAL_TYPES.contains(normalizedSignalType)) {
            throw new BadRequestException("Unsupported WebRTC signal type");
        }

        return normalizedSignalType;
    }

    private void validateParticipantAccess(
            Long senderUserId,
            WebRtcSignalRequest request
    ) {
        if (request.getSessionType() == SessionType.DEBATE) {
            DebateSession debateSession = debateSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Debate session not found"));

            validateParticipants(
                    senderUserId,
                    request.getReceiverUserId(),
                    debateSession.getParticipantA().getId(),
                    debateSession.getParticipantB().getId()
            );
            return;
        }

        if (request.getSessionType() == SessionType.ROLEPLAY) {
            RoleplaySession roleplaySession = roleplaySessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Roleplay session not found"));

            validateParticipants(
                    senderUserId,
                    request.getReceiverUserId(),
                    roleplaySession.getParticipantA().getId(),
                    roleplaySession.getParticipantB().getId()
            );
            return;
        }

        throw new BadRequestException("WebRTC signaling is only supported for Debate and Roleplay");
    }

    private void validateParticipants(
            Long senderUserId,
            Long receiverUserId,
            Long participantAId,
            Long participantBId
    ) {
        boolean senderBelongsToSession = senderUserId.equals(participantAId) || senderUserId.equals(participantBId);

        if (!senderBelongsToSession) {
            throw new AccessDeniedException("Access denied");
        }

        if (receiverUserId == null) {
            return;
        }

        boolean receiverBelongsToSession = receiverUserId.equals(participantAId)
                || receiverUserId.equals(participantBId);

        if (!receiverBelongsToSession || receiverUserId.equals(senderUserId)) {
            throw new BadRequestException("Invalid WebRTC receiver");
        }
    }

    private String toEventType(String signalType) {
        if (OFFER.equals(signalType)) {
            return EventPublisherService.WEBRTC_OFFER;
        }

        if (ANSWER.equals(signalType)) {
            return EventPublisherService.WEBRTC_ANSWER;
        }

        return EventPublisherService.ICE_CANDIDATE;
    }
}
