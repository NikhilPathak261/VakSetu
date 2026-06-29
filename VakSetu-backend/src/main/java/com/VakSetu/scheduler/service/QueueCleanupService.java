package com.vaksetu.scheduler.service;

import com.vaksetu.common.constants.AppConstants;
import com.vaksetu.matchmaking.service.DebateQueueService;
import com.vaksetu.matchmaking.service.RoleplayQueueService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueCleanupService {

    private final DebateQueueService debateQueueService;
    private final RoleplayQueueService roleplayQueueService;

    public int expireDebateQueueEntries() {
        return debateQueueService.removeEntriesJoinedBefore(queueCutoffTime());
    }

    public int expireRoleplayQueueEntries() {
        return roleplayQueueService.removeEntriesJoinedBefore(queueCutoffTime());
    }

    private LocalDateTime queueCutoffTime() {
        return LocalDateTime.now().minusMinutes(AppConstants.QUEUE_ENTRY_TTL_MINUTES);
    }
}
