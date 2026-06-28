package com.vaksetu.websocket;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.vaksetu.common.enums.DebateSide;
import com.vaksetu.common.enums.Rank;
import com.vaksetu.common.enums.Role;
import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.common.enums.SessionType;
import com.vaksetu.debate.entity.DebateSession;
import com.vaksetu.debate.repository.DebateSessionRepository;
import com.vaksetu.roleplay.repository.RoleplaySessionRepository;
import com.vaksetu.topic.entity.Topic;
import com.vaksetu.user.entity.User;
import com.vaksetu.websocket.dto.WebRtcSignalRequest;
import com.vaksetu.websocket.service.EventPublisherService;
import com.vaksetu.websocket.service.WebRtcSignalingService;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

class WebRtcSignalingServiceTest {

    private final DebateSessionRepository debateSessionRepository =
            org.mockito.Mockito.mock(DebateSessionRepository.class);
    private final RoleplaySessionRepository roleplaySessionRepository =
            org.mockito.Mockito.mock(RoleplaySessionRepository.class);
    private final EventPublisherService eventPublisherService =
            org.mockito.Mockito.mock(EventPublisherService.class);

    private final WebRtcSignalingService webRtcSignalingService = new WebRtcSignalingService(
            debateSessionRepository,
            roleplaySessionRepository,
            eventPublisherService
    );

    @Test
    void forwardSignalPublishesForDebateParticipants() {
        DebateSession debateSession = createDebateSession(10L, 20L);
        when(debateSessionRepository.findById(1L)).thenReturn(Optional.of(debateSession));

        webRtcSignalingService.forwardSignal(10L, WebRtcSignalRequest.builder()
                .sessionId(1L)
                .sessionType(SessionType.DEBATE)
                .receiverUserId(20L)
                .signalType("offer")
                .signalData(Map.of("sdp", "mock-sdp"))
                .build());

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisherService).publishWebRtcSignal(
                eq(EventPublisherService.WEBRTC_OFFER),
                eq(1L),
                eq(10L),
                payloadCaptor.capture()
        );
    }

    @Test
    void forwardSignalRejectsUsersOutsideSession() {
        DebateSession debateSession = createDebateSession(10L, 20L);
        when(debateSessionRepository.findById(1L)).thenReturn(Optional.of(debateSession));

        assertThatThrownBy(() -> webRtcSignalingService.forwardSignal(30L, WebRtcSignalRequest.builder()
                .sessionId(1L)
                .sessionType(SessionType.DEBATE)
                .signalType("ANSWER")
                .signalData(Map.of("sdp", "mock-sdp"))
                .build()))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(eventPublisherService);
    }

    private DebateSession createDebateSession(
            Long participantAId,
            Long participantBId
    ) {
        return DebateSession.builder()
                .id(1L)
                .topic(Topic.builder().id(1L).title("Topic").category("General").active(true).build())
                .participantA(createUser(participantAId))
                .participantB(createUser(participantBId))
                .sideA(DebateSide.FOR)
                .sideB(DebateSide.AGAINST)
                .status(SessionStatus.ACTIVE)
                .currentRound(1)
                .totalRounds(3)
                .preparationSeconds(60)
                .roundDurationSeconds(120)
                .build();
    }

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .name("User " + id)
                .email("user" + id + "@example.com")
                .passwordHash("hashed")
                .overallScore(50.0)
                .reputation(50)
                .rank(Rank.CONVERSATIONALIST)
                .role(Role.USER)
                .totalStars(0)
                .topContributorFinishes(0)
                .highestSessionStars(0)
                .sessionsCompleted(0)
                .debatesCompleted(0)
                .roleplaysCompleted(0)
                .gdSessionsJoined(0)
                .build();
    }
}
