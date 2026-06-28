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
public class UserStatisticsResponse {

    private Integer totalStars;
    private Integer highestSessionStars;
    private Integer topContributorFinishes;
    private Integer sessionsCompleted;
    private Integer debatesCompleted;
    private Integer roleplaysCompleted;
    private Integer gdSessionsJoined;
}
