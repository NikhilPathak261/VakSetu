package com.vaksetu.user.dto;

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
public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;

    private Double overallScore;
    private Integer reputation;

    private Rank rank;
    private BadgeType contributorBadge;

    private Double fluency;
    private Double pronunciation;
    private Double grammar;
    private Double confidence;
    private Double empathy;
    private Double listening;
    private Double engagement;
}
