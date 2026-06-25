package com.vaksetu.debate.controller;

import com.vaksetu.debate.dto.CreateDebateSessionRequest;
import com.vaksetu.debate.dto.DebateSessionResponse;
import com.vaksetu.debate.service.DebateSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debates")
@RequiredArgsConstructor
public class DebateSessionController {

    private final DebateSessionService debateSessionService;

    @PostMapping
    public DebateSessionResponse createSession(
            @Valid @RequestBody CreateDebateSessionRequest request
    ) {
        return debateSessionService.createSession(request);
    }

    @GetMapping("/{id}")
    public DebateSessionResponse getSession(
            @PathVariable Long id
    ) {
        return debateSessionService.getSession(id);
    }

    @PostMapping("/{id}/start")
    public DebateSessionResponse startSession(
            @PathVariable Long id
    ) {
        return debateSessionService.startSession(id);
    }

    @PostMapping("/{id}/complete")
    public DebateSessionResponse completeSession(
            @PathVariable Long id
    ) {
        return debateSessionService.completeSession(id);
    }
}
