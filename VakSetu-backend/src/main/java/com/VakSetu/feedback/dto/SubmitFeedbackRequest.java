package com.vaksetu.feedback.dto;

import com.vaksetu.common.enums.SessionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class SubmitFeedbackRequest {

    @NotNull
    private Long sessionId;

    @NotNull
    private SessionType sessionType;

    @NotNull
    private Long targetUserId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer fluencyRating;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer pronunciationRating;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer grammarRating;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer confidenceRating;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer empathyRating;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer listeningRating;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer engagementRating;
}
