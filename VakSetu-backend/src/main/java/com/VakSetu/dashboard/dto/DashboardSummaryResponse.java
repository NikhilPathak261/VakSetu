package com.vaksetu.dashboard.dto;

import com.vaksetu.common.enums.BadgeType;
import com.vaksetu.common.enums.Rank;
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
public class DashboardSummaryResponse {

    private Long userId;
    private String name;
    private Double overallScore;
    private Integer reputation;
    private Rank rank;
    private BadgeType contributorBadge;
    private SkillSnapshotResponse skills;
    private UserStatisticsResponse statistics;
}
