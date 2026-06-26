package com.vaksetu.roleplay.service;

import com.vaksetu.roleplay.dto.RoleplayRuntimeResponse;
import com.vaksetu.roleplay.entity.RoleplaySession;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleplayRuntimeService {

    private final RoleplaySessionService roleplaySessionService;

    @Transactional(readOnly = true)
    public RoleplayRuntimeResponse getRuntime(Long sessionId) {
        return mapToRuntimeResponse(roleplaySessionService.loadSession(sessionId));
    }

    @Transactional
    public RoleplayRuntimeResponse startPreparation(Long sessionId) {
        roleplaySessionService.startPreparation(sessionId);
        return mapToRuntimeResponse(roleplaySessionService.loadSession(sessionId));
    }

    @Transactional
    public RoleplayRuntimeResponse startRoleplay(Long sessionId) {
        roleplaySessionService.startRoleplay(sessionId);
        return mapToRuntimeResponse(roleplaySessionService.loadSession(sessionId));
    }

    @Transactional
    public RoleplayRuntimeResponse completeRoleplay(Long sessionId) {
        roleplaySessionService.completeRoleplay(sessionId);
        return mapToRuntimeResponse(roleplaySessionService.loadSession(sessionId));
    }

    private RoleplayRuntimeResponse mapToRuntimeResponse(RoleplaySession session) {
        return RoleplayRuntimeResponse.builder()
                .sessionId(session.getId())
                .status(session.getStatus())
                .currentPhase(session.getCurrentPhase())
                .preparationSeconds(session.getPreparationSeconds())
                .sessionDurationSeconds(session.getSessionDurationSeconds())
                .remainingSeconds(calculateRemainingSeconds(session))
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .build();
    }

    private Integer calculateRemainingSeconds(RoleplaySession session) {
        if (session.getEndTime() == null) {
            return 0;
        }

        long remainingSeconds = Duration.between(LocalDateTime.now(), session.getEndTime()).getSeconds();
        return (int) Math.max(remainingSeconds, 0);
    }
}
