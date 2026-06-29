package com.vaksetu.debate.dto;

import com.vaksetu.common.enums.DebateSide;
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
public class DebateSessionResponse {

    private Long id;

    private Long topicId;
    private String topicTitle;

    private Long participantAId;
    private String participantAName;

    private Long participantBId;
    private String participantBName;

    private DebateSide sideA;
    private DebateSide sideB;

    private SessionStatus status;

    private Integer currentRound;
    private Integer totalRounds;
    private LocalDateTime roundStartTime;
    private LocalDateTime roundEndTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
