package com.vaksetu.roleplay.service;

import com.vaksetu.common.mapper.RoleplayMapper;
import com.vaksetu.roleplay.dto.RoleplayRuntimeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleplayRuntimeService {

    private final RoleplaySessionService roleplaySessionService;

    @Transactional(readOnly = true)
    public RoleplayRuntimeResponse getRuntime(Long sessionId) {
        return RoleplayMapper.toRuntimeResponse(roleplaySessionService.loadSession(sessionId));
    }

    @Transactional
    public RoleplayRuntimeResponse startPreparation(Long sessionId) {
        roleplaySessionService.startPreparation(sessionId);
        return RoleplayMapper.toRuntimeResponse(roleplaySessionService.loadSession(sessionId));
    }

    @Transactional
    public RoleplayRuntimeResponse startRoleplay(Long sessionId) {
        roleplaySessionService.startRoleplay(sessionId);
        return RoleplayMapper.toRuntimeResponse(roleplaySessionService.loadSession(sessionId));
    }

    @Transactional
    public RoleplayRuntimeResponse completeRoleplay(Long sessionId) {
        roleplaySessionService.completeRoleplay(sessionId);
        return RoleplayMapper.toRuntimeResponse(roleplaySessionService.loadSession(sessionId));
    }
}
