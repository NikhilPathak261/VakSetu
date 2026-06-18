package com.vaksetu.matchmaking.controller;

import com.vaksetu.matchmaking.dto.JoinDebateQueueRequest;
import com.vaksetu.matchmaking.dto.QueueStatusResponse;
import com.vaksetu.matchmaking.service.DebateQueueService;
import com.vaksetu.matchmaking.service.MatchmakingService;
import com.vaksetu.matchmaking.service.RoleplayQueueService;
import com.vaksetu.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matchmaking")
@RequiredArgsConstructor
public class MatchmakingController {

    private final DebateQueueService debateQueueService;
    private final RoleplayQueueService roleplayQueueService;
    private final MatchmakingService matchmakingService;

    @PostMapping("/debate/join")
    public ResponseEntity<String> joinDebateQueue(
            @Valid @RequestBody JoinDebateQueueRequest request
    ) {
        Long userId = getAuthenticatedUserId();

        debateQueueService.addUser(userId, request.getTopicId());
        matchmakingService.findDebateMatch(userId, request.getTopicId());

        return ResponseEntity.ok("Joined debate queue");
    }

    @PostMapping("/debate/leave")
    public ResponseEntity<String> leaveDebateQueue() {
        Long userId = getAuthenticatedUserId();

        debateQueueService.removeUser(userId);

        return ResponseEntity.ok("Left debate queue");
    }

    @GetMapping("/debate/status")
    public QueueStatusResponse getDebateQueueStatus() {
        Long userId = getAuthenticatedUserId();
        boolean queued = debateQueueService.getAllEntries()
                .stream()
                .anyMatch(entry -> entry.getUserId().equals(userId));

        return QueueStatusResponse.builder()
                .queued(queued)
                .queueType("DEBATE")
                .queueSize(debateQueueService.getAllEntries().size())
                .build();
    }

    @PostMapping("/roleplay/join")
    public ResponseEntity<String> joinRoleplayQueue() {
        Long userId = getAuthenticatedUserId();

        roleplayQueueService.addUser(userId);
        matchmakingService.findRoleplayMatch(userId);

        return ResponseEntity.ok("Joined roleplay queue");
    }

    @PostMapping("/roleplay/leave")
    public ResponseEntity<String> leaveRoleplayQueue() {
        Long userId = getAuthenticatedUserId();

        roleplayQueueService.removeUser(userId);

        return ResponseEntity.ok("Left roleplay queue");
    }

    @GetMapping("/roleplay/status")
    public QueueStatusResponse getRoleplayQueueStatus() {
        Long userId = getAuthenticatedUserId();
        boolean queued = roleplayQueueService.getAllEntries()
                .stream()
                .anyMatch(entry -> entry.getUserId().equals(userId));

        return QueueStatusResponse.builder()
                .queued(queued)
                .queueType("ROLEPLAY")
                .queueSize(roleplayQueueService.getAllEntries().size())
                .build();
    }

    private Long getAuthenticatedUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return userDetails.getId();
    }
}
