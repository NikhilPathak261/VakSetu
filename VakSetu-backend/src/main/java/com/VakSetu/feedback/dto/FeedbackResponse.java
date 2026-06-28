package com.vaksetu.feedback.dto;

import com.vaksetu.common.enums.SessionType;
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
public class FeedbackResponse {

    private Long feedbackId;
    private Long sessionId;
    private SessionType sessionType;
    private Long evaluatorId;
    private Long targetUserId;
    private Boolean sessionCompleted;
    private String message;
}
