package com.vaksetu.common.mapper;

import com.vaksetu.roleplay.dto.RoleplayScenarioResponse;
import com.vaksetu.roleplay.dto.RoleplaySessionResponse;
import com.vaksetu.roleplay.entity.RoleplayScenario;
import com.vaksetu.roleplay.entity.RoleplaySession;

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
}
