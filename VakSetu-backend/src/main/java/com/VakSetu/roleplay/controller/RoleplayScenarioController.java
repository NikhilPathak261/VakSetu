package com.vaksetu.roleplay.controller;

import com.vaksetu.roleplay.dto.CreateRoleplayScenarioRequest;
import com.vaksetu.roleplay.dto.RoleplayScenarioResponse;
import com.vaksetu.roleplay.service.RoleplayScenarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roleplay/scenarios")
@RequiredArgsConstructor
public class RoleplayScenarioController {

    private final RoleplayScenarioService roleplayScenarioService;

    @GetMapping
    public List<RoleplayScenarioResponse> getActiveScenarios() {
        return roleplayScenarioService.getActiveScenarios();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RoleplayScenarioResponse createScenario(
            @Valid @RequestBody CreateRoleplayScenarioRequest request
    ) {
        return roleplayScenarioService.createScenario(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateScenario(@PathVariable Long id) {
        roleplayScenarioService.deactivateScenario(id);
        return ResponseEntity.noContent().build();
    }
}
