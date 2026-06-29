package com.vaksetu.scheduler.service;

import com.vaksetu.common.constants.AppConstants;
import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.debate.entity.DebateSession;
import com.vaksetu.debate.repository.DebateSessionRepository;
import com.vaksetu.roleplay.entity.RoleplaySession;
import com.vaksetu.roleplay.repository.RoleplaySessionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionCleanupService {

    private static final List<SessionStatus> DEBATE_CLEANUP_STATUSES = List.of(
            SessionStatus.MATCHED,
            SessionStatus.PREPARATION,
            SessionStatus.ROUND_1,
            SessionStatus.ROUND_2,
            SessionStatus.ROUND_3
    );
    private static final List<SessionStatus> ROLEPLAY_CLEANUP_STATUSES = List.of(
            SessionStatus.PREPARATION,
            SessionStatus.ACTIVE
    );

    private final DebateSessionRepository debateSessionRepository;
    private final RoleplaySessionRepository roleplaySessionRepository;

    @Transactional
    public int cancelStaleDebateSessions() {
        List<DebateSession> sessions = debateSessionRepository.findByStatusInAndUpdatedAtBefore(
                DEBATE_CLEANUP_STATUSES,
                staleSessionCutoffTime()
        );

        sessions.forEach(session -> {
            session.setStatus(SessionStatus.CANCELLED);
            session.setEndTime(LocalDateTime.now());
        });

        return sessions.size();
    }

    @Transactional
    public int cancelStaleRoleplaySessions() {
        List<RoleplaySession> sessions = roleplaySessionRepository.findByStatusInAndUpdatedAtBefore(
                ROLEPLAY_CLEANUP_STATUSES,
                staleSessionCutoffTime()
        );

        sessions.forEach(session -> {
            session.setStatus(SessionStatus.CANCELLED);
            session.setCurrentPhase(SessionStatus.CANCELLED);
            session.setEndTime(LocalDateTime.now());
        });

        return sessions.size();
    }

    private LocalDateTime staleSessionCutoffTime() {
        return LocalDateTime.now().minusMinutes(AppConstants.STALE_SESSION_TTL_MINUTES);
    }
}
