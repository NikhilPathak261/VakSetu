package com.vaksetu.dashboard.dto;

import com.vaksetu.common.enums.SessionType;
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
public class SkillHistoryResponse {

    private Long id;
    private String skillName;
    private Double oldValue;
    private Double newValue;
    private Long sessionId;
    private SessionType sessionType;
    private LocalDateTime createdAt;
}
