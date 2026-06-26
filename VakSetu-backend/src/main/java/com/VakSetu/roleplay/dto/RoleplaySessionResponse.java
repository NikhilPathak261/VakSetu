package com.vaksetu.roleplay.dto;

import com.vaksetu.common.enums.SessionStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleplaySessionResponse {

    private Long sessionId;
    private Long scenarioId;
    private String scenarioTitle;
    private String scenarioDescription;
    private Long participantAId;
    private String participantAName;
    private Long participantBId;
    private String participantBName;
    private String assignedRoleA;
    private String assignedRoleB;
    private SessionStatus status;
    private SessionStatus currentPhase;
    private Integer preparationSeconds;
    private Integer sessionDurationSeconds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
