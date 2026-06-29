package com.vaksetu.scheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.debate.entity.DebateSession;
import com.vaksetu.debate.repository.DebateSessionRepository;
import com.vaksetu.roleplay.entity.RoleplaySession;
import com.vaksetu.roleplay.repository.RoleplaySessionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SessionCleanupServiceTest {

    private final DebateSessionRepository debateSessionRepository =
            org.mockito.Mockito.mock(DebateSessionRepository.class);
    private final RoleplaySessionRepository roleplaySessionRepository =
            org.mockito.Mockito.mock(RoleplaySessionRepository.class);
    private final SessionCleanupService sessionCleanupService = new SessionCleanupService(
            debateSessionRepository,
            roleplaySessionRepository
    );

    @Test
    void cancelsStaleDebateSessionsWithoutChangingScores() {
        DebateSession session = DebateSession.builder()
                .id(1L)
                .status(SessionStatus.ROUND_2)
                .build();
        when(debateSessionRepository.findByStatusInAndUpdatedAtBefore(anyCollection(), any(LocalDateTime.class)))
                .thenReturn(List.of(session));

        int cancelledSessions = sessionCleanupService.cancelStaleDebateSessions();

        assertThat(cancelledSessions).isEqualTo(1);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.CANCELLED);
        assertThat(session.getEndTime()).isNotNull();
    }

    @Test
    void cancelsStaleRoleplaySessionsAndPhase() {
        RoleplaySession session = RoleplaySession.builder()
                .id(1L)
                .status(SessionStatus.ACTIVE)
                .currentPhase(SessionStatus.ACTIVE)
                .build();
        when(roleplaySessionRepository.findByStatusInAndUpdatedAtBefore(anyCollection(), any(LocalDateTime.class)))
                .thenReturn(List.of(session));

        int cancelledSessions = sessionCleanupService.cancelStaleRoleplaySessions();

        assertThat(cancelledSessions).isEqualTo(1);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.CANCELLED);
        assertThat(session.getCurrentPhase()).isEqualTo(SessionStatus.CANCELLED);
        assertThat(session.getEndTime()).isNotNull();
    }
}
