package com.vaksetu.roleplay.controller;

import com.vaksetu.roleplay.dto.CreateRoleplaySessionRequest;
import com.vaksetu.roleplay.dto.RoleplaySessionResponse;
import com.vaksetu.roleplay.service.RoleplaySessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roleplay")
@RequiredArgsConstructor
public class RoleplaySessionController {

    private final RoleplaySessionService roleplaySessionService;

    @PostMapping
    public RoleplaySessionResponse createSession(
            @Valid @RequestBody CreateRoleplaySessionRequest request
    ) {
        return roleplaySessionService.createSession(request);
    }

    @GetMapping("/{id}")
    public RoleplaySessionResponse getSession(
            @PathVariable Long id
    ) {
        return roleplaySessionService.getSession(id);
    }

    @PostMapping("/{id}/start")
    public RoleplaySessionResponse startRoleplay(
            @PathVariable Long id
    ) {
        return roleplaySessionService.startRoleplay(id);
    }

    @PostMapping("/{id}/complete")
    public RoleplaySessionResponse completeRoleplay(
            @PathVariable Long id
    ) {
        return roleplaySessionService.completeRoleplay(id);
    }
}
