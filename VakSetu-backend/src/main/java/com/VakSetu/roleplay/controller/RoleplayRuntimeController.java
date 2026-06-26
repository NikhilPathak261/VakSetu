package com.vaksetu.roleplay.controller;

import com.vaksetu.roleplay.dto.RoleplayRuntimeResponse;
import com.vaksetu.roleplay.service.RoleplayRuntimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roleplay/runtime")
@RequiredArgsConstructor
public class RoleplayRuntimeController {

    private final RoleplayRuntimeService roleplayRuntimeService;

    @PostMapping("/{id}/prepare")
    public RoleplayRuntimeResponse startPreparation(
            @PathVariable Long id
    ) {
        return roleplayRuntimeService.startPreparation(id);
    }

    @PostMapping("/{id}/start")
    public RoleplayRuntimeResponse startRoleplay(
            @PathVariable Long id
    ) {
        return roleplayRuntimeService.startRoleplay(id);
    }

    @PostMapping("/{id}/complete")
    public RoleplayRuntimeResponse completeRoleplay(
            @PathVariable Long id
    ) {
        return roleplayRuntimeService.completeRoleplay(id);
    }

    @GetMapping("/{id}")
    public RoleplayRuntimeResponse getRuntime(
            @PathVariable Long id
    ) {
        return roleplayRuntimeService.getRuntime(id);
    }
}
