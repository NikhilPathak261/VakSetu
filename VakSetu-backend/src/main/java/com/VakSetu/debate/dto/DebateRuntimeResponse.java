package com.vaksetu.debate.dto;

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
public class DebateRuntimeResponse {

    private Long sessionId;
    private SessionStatus status;
    private Integer currentRound;
    private Integer totalRounds;
    private Long currentSpeakerId;
    private String currentSpeakerName;
    private LocalDateTime roundStartTime;
    private LocalDateTime roundEndTime;
    private Integer preparationSeconds;
    private Integer roundDurationSeconds;
}
