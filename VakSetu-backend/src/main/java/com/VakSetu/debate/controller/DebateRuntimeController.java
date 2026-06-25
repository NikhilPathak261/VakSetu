package com.vaksetu.debate.controller;

import com.vaksetu.debate.dto.DebateRuntimeResponse;
import com.vaksetu.debate.service.DebateRuntimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debates/runtime")
@RequiredArgsConstructor
public class DebateRuntimeController {

    private final DebateRuntimeService debateRuntimeService;

    @PostMapping("/{id}/prepare")
    public DebateRuntimeResponse moveToPreparation(
            @PathVariable Long id
    ) {
        return debateRuntimeService.moveToPreparation(id);
    }

    @PostMapping("/{id}/round1")
    public DebateRuntimeResponse startRoundOne(
            @PathVariable Long id
    ) {
        return debateRuntimeService.startRoundOne(id);
    }

    @PostMapping("/{id}/round2")
    public DebateRuntimeResponse startRoundTwo(
            @PathVariable Long id
    ) {
        return debateRuntimeService.startRoundTwo(id);
    }

    @PostMapping("/{id}/round3")
    public DebateRuntimeResponse startRoundThree(
            @PathVariable Long id
    ) {
        return debateRuntimeService.startRoundThree(id);
    }

    @PostMapping("/{id}/complete")
    public DebateRuntimeResponse completeDebate(
            @PathVariable Long id
    ) {
        return debateRuntimeService.completeDebate(id);
    }

    @GetMapping("/{id}")
    public DebateRuntimeResponse getRuntime(
            @PathVariable Long id
    ) {
        return debateRuntimeService.getRuntime(id);
    }
}
