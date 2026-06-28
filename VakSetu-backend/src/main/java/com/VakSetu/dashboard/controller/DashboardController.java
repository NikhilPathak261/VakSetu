package com.vaksetu.dashboard.controller;

import com.vaksetu.dashboard.dto.DashboardSummaryResponse;
import com.vaksetu.dashboard.dto.ReputationHistoryResponse;
import com.vaksetu.dashboard.dto.SkillHistoryResponse;
import com.vaksetu.dashboard.service.DashboardService;
import com.vaksetu.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/me")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary(getAuthenticatedUserId());
    }

    @GetMapping("/me/skill-history")
    public List<SkillHistoryResponse> getSkillHistory() {
        return dashboardService.getSkillHistory(getAuthenticatedUserId());
    }

    @GetMapping("/me/reputation-history")
    public List<ReputationHistoryResponse> getReputationHistory() {
        return dashboardService.getReputationHistory(getAuthenticatedUserId());
    }

    private Long getAuthenticatedUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return userDetails.getId();
    }
}
