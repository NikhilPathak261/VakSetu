package com.vaksetu.scheduler.service;

import com.vaksetu.scheduler.dto.CleanupResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CleanupOrchestratorService {

    private final QueueCleanupService queueCleanupService;
    private final SessionCleanupService sessionCleanupService;

    public CleanupResult runCleanup() {
        return CleanupResult.builder()
                .expiredDebateQueueEntries(queueCleanupService.expireDebateQueueEntries())
                .expiredRoleplayQueueEntries(queueCleanupService.expireRoleplayQueueEntries())
                .cancelledDebateSessions(sessionCleanupService.cancelStaleDebateSessions())
                .cancelledRoleplaySessions(sessionCleanupService.cancelStaleRoleplaySessions())
                .build();
    }
}
