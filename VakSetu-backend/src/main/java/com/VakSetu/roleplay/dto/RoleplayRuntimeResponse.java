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
public class RoleplayRuntimeResponse {

    private Long sessionId;
    private SessionStatus status;
    private SessionStatus currentPhase;
    private Integer preparationSeconds;
    private Integer sessionDurationSeconds;
    private Integer remainingSeconds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
