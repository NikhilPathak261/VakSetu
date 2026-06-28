package com.vaksetu.gd.dto;

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
public class GDSessionResponse {

    private Long sessionId;
    private String topic;
    private Long creatorId;
    private String creatorName;
    private SessionStatus status;
    private Integer currentParticipants;
    private Integer maxParticipants;
    private LocalDateTime createdAt;
}
