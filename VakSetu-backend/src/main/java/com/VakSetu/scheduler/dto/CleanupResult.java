package com.vaksetu.scheduler.dto;

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
public class CleanupResult {

    private int expiredDebateQueueEntries;
    private int expiredRoleplayQueueEntries;
    private int cancelledDebateSessions;
    private int cancelledRoleplaySessions;
}
