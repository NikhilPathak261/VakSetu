package com.vaksetu.dashboard.dto;

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
public class SkillSnapshotResponse {

    private Double fluency;
    private Double pronunciation;
    private Double grammar;
    private Double confidence;
    private Double empathy;
    private Double listening;
    private Double engagement;
}
