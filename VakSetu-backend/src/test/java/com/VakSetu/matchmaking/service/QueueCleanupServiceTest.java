package com.vaksetu.matchmaking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaksetu.matchmaking.queue.DebateQueueEntry;
import com.vaksetu.matchmaking.queue.RoleplayQueueEntry;
import com.vaksetu.scheduler.service.QueueCleanupService;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class QueueCleanupServiceTest {

    private final DebateQueueService debateQueueService = new DebateQueueService(null, null);
    private final RoleplayQueueService roleplayQueueService = new RoleplayQueueService(null, null);
    private final QueueCleanupService queueCleanupService = new QueueCleanupService(
            debateQueueService,
            roleplayQueueService
    );

    @Test
    void expiresOnlyOldDebateQueueEntries() {
        Map<Long, DebateQueueEntry> queue = getDebateQueue();
        queue.put(1L, DebateQueueEntry.builder()
                .userId(1L)
                .topicId(10L)
                .joinedAt(LocalDateTime.now().minusMinutes(20))
                .build());
        queue.put(2L, DebateQueueEntry.builder()
                .userId(2L)
                .topicId(10L)
                .joinedAt(LocalDateTime.now().minusMinutes(5))
                .build());

        int expiredEntries = queueCleanupService.expireDebateQueueEntries();

        assertThat(expiredEntries).isEqualTo(1);
        assertThat(debateQueueService.getAllEntries())
                .extracting(DebateQueueEntry::getUserId)
                .containsExactly(2L);
    }

    @Test
    void expiresOnlyOldRoleplayQueueEntries() {
        Map<Long, RoleplayQueueEntry> queue = getRoleplayQueue();
        queue.put(1L, RoleplayQueueEntry.builder()
                .userId(1L)
                .joinedAt(LocalDateTime.now().minusMinutes(20))
                .build());
        queue.put(2L, RoleplayQueueEntry.builder()
                .userId(2L)
                .joinedAt(LocalDateTime.now().minusMinutes(5))
                .build());

        int expiredEntries = queueCleanupService.expireRoleplayQueueEntries();

        assertThat(expiredEntries).isEqualTo(1);
        assertThat(roleplayQueueService.getAllEntries())
                .extracting(RoleplayQueueEntry::getUserId)
                .containsExactly(2L);
    }

    @SuppressWarnings("unchecked")
    private Map<Long, DebateQueueEntry> getDebateQueue() {
        return (Map<Long, DebateQueueEntry>) ReflectionTestUtils.getField(debateQueueService, "queue");
    }

    @SuppressWarnings("unchecked")
    private Map<Long, RoleplayQueueEntry> getRoleplayQueue() {
        return (Map<Long, RoleplayQueueEntry>) ReflectionTestUtils.getField(roleplayQueueService, "queue");
    }
}
