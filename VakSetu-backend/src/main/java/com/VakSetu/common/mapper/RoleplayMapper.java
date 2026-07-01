package com.vaksetu.common.mapper;

import com.vaksetu.roleplay.dto.RoleplayScenarioResponse;
import com.vaksetu.roleplay.dto.RoleplayRuntimeResponse;
import com.vaksetu.roleplay.dto.RoleplaySessionResponse;
import com.vaksetu.roleplay.entity.RoleplayScenario;
import com.vaksetu.roleplay.entity.RoleplaySession;
import java.time.Duration;
import java.time.LocalDateTime;

public final class RoleplayMapper {

    private RoleplayMapper() {
    }

    public static RoleplayScenarioResponse toScenarioResponse(RoleplayScenario scenario) {
        return RoleplayScenarioResponse.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .description(scenario.getDescription())
                .roleA(scenario.getRoleA())
                .roleB(scenario.getRoleB())
                .difficulty(scenario.getDifficulty())
                .active(scenario.getActive())
                .build();
    }

    public static RoleplaySessionResponse toSessionResponse(RoleplaySession session) {
        return RoleplaySessionResponse.builder()
                .sessionId(session.getId())
                .scenarioId(session.getScenario().getId())
                .scenarioTitle(session.getScenario().getTitle())
                .scenarioDescription(session.getScenario().getDescription())
                .participantAId(session.getParticipantA().getId())
                .participantAName(session.getParticipantA().getName())
                .participantBId(session.getParticipantB().getId())
                .participantBName(session.getParticipantB().getName())
                .assignedRoleA(session.getAssignedRoleA())
                .assignedRoleB(session.getAssignedRoleB())
                .status(session.getStatus())
                .currentPhase(session.getCurrentPhase())
                .preparationSeconds(session.getPreparationSeconds())
                .sessionDurationSeconds(session.getSessionDurationSeconds())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .build();
    }

    public static RoleplayRuntimeResponse toRuntimeResponse(RoleplaySession session) {
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

    private static Integer calculateRemainingSeconds(RoleplaySession session) {
        if (session.getEndTime() == null) {
            return 0;
        }

        long remainingSeconds = Duration.between(LocalDateTime.now(), session.getEndTime()).getSeconds();
        return (int) Math.max(remainingSeconds, 0);
    }
}
