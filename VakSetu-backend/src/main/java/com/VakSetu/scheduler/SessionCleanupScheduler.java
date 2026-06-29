package com.vaksetu.scheduler;

import com.vaksetu.common.constants.AppConstants;
import com.vaksetu.scheduler.service.CleanupOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    private final CleanupOrchestratorService cleanupOrchestratorService;

    @Scheduled(fixedDelay = AppConstants.CLEANUP_FIXED_DELAY_MS)
    public void runCleanup() {
        cleanupOrchestratorService.runCleanup();
    }
}
