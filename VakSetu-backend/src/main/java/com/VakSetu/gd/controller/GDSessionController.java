package com.vaksetu.gd.controller;

import com.vaksetu.gd.dto.CreateGDSessionRequest;
import com.vaksetu.gd.dto.GDLeaderboardResponse;
import com.vaksetu.gd.dto.GDSessionResponse;
import com.vaksetu.gd.dto.GiveStarRequest;
import com.vaksetu.gd.dto.JoinLeaveRoomResponse;
import com.vaksetu.gd.dto.MarkSpokenResponse;
import com.vaksetu.gd.dto.StarResponse;
import com.vaksetu.gd.service.GDLeaderboardService;
import com.vaksetu.gd.service.GDSessionService;
import com.vaksetu.gd.service.GDStarService;
import com.vaksetu.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gd")
@RequiredArgsConstructor
public class GDSessionController {

    private final GDSessionService gdSessionService;
    private final GDStarService gdStarService;
    private final GDLeaderboardService gdLeaderboardService;

    @PostMapping
    public GDSessionResponse createRoom(
            @Valid @RequestBody CreateGDSessionRequest request
    ) {
        return gdSessionService.createRoom(getAuthenticatedUserId(), request);
    }

    @GetMapping("/{id}")
    public GDSessionResponse getRoom(
            @PathVariable Long id
    ) {
        return gdSessionService.getRoom(id);
    }

    @GetMapping("/active")
    public List<GDSessionResponse> getActiveRooms() {
        return gdSessionService.getActiveRooms();
    }

    @PostMapping("/{sessionId}/join")
    public JoinLeaveRoomResponse joinRoom(
            @PathVariable Long sessionId
    ) {
        return gdSessionService.joinRoom(getAuthenticatedUserId(), sessionId);
    }

    @PostMapping("/{sessionId}/leave")
    public JoinLeaveRoomResponse leaveRoom(
            @PathVariable Long sessionId
    ) {
        return gdSessionService.leaveRoom(getAuthenticatedUserId(), sessionId);
    }

    @PostMapping("/{sessionId}/spoken")
    public MarkSpokenResponse markSpoken(
            @PathVariable Long sessionId
    ) {
        return gdSessionService.markSpoken(getAuthenticatedUserId(), sessionId);
    }

    @PostMapping("/star")
    public StarResponse giveStar(
            @Valid @RequestBody GiveStarRequest request
    ) {
        return gdStarService.giveStar(getAuthenticatedUserId(), request);
    }

    @PostMapping("/{sessionId}/close")
    public GDSessionResponse closeRoom(
            @PathVariable Long sessionId
    ) {
        return gdSessionService.closeRoom(getAuthenticatedUserId(), sessionId);
    }

    @GetMapping("/{sessionId}/leaderboard")
    public GDLeaderboardResponse getLeaderboard(
            @PathVariable Long sessionId
    ) {
        return gdLeaderboardService.getLeaderboard(sessionId);
    }

    private Long getAuthenticatedUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return userDetails.getId();
    }
}
