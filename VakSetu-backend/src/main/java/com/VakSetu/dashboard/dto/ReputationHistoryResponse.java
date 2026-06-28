package com.vaksetu.dashboard.dto;

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
public class ReputationHistoryResponse {

    private Long id;
    private Integer changeAmount;
    private String reason;
    private LocalDateTime createdAt;
}
