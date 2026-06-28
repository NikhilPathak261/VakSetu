package com.vaksetu.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillRatingScores {

    private Double fluency;
    private Double pronunciation;
    private Double grammar;
    private Double confidence;
    private Double empathy;
    private Double listening;
    private Double engagement;
}
