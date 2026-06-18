package com.vaksetu.matchmaking.queue;

import com.vaksetu.matchmaking.dto.UserSkillSnapshot;
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
public class RoleplayQueueEntry {

    private Long userId;
    private UserSkillSnapshot skillSnapshot;
    private LocalDateTime joinedAt;
}
