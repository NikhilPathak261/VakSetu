package com.vaksetu.roleplay.service;

import com.vaksetu.common.constants.AppConstants;
import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.exception.BadRequestException;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.roleplay.dto.CreateRoleplaySessionRequest;
import com.vaksetu.roleplay.dto.RoleplaySessionResponse;
import com.vaksetu.roleplay.entity.RoleplayScenario;
import com.vaksetu.roleplay.entity.RoleplaySession;
import com.vaksetu.roleplay.repository.RoleplayScenarioRepository;
import com.vaksetu.roleplay.repository.RoleplaySessionRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleplaySessionService {

    private final RoleplaySessionRepository roleplaySessionRepository;
    private final RoleplayScenarioRepository roleplayScenarioRepository;
    private final UserRepository userRepository;

    @Transactional
    public RoleplaySessionResponse createSession(CreateRoleplaySessionRequest request) {
        User participantA = userRepository.findById(request.getParticipantAId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant A not found"));
        User participantB = userRepository.findById(request.getParticipantBId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant B not found"));
        RoleplayScenario scenario = selectRandomScenario();
        boolean keepScenarioRoles = ThreadLocalRandom.current().nextBoolean();
        LocalDateTime now = LocalDateTime.now();

        RoleplaySession roleplaySession = RoleplaySession.builder()
                .scenario(scenario)
                .participantA(participantA)
                .participantB(participantB)
                .assignedRoleA(keepScenarioRoles ? scenario.getRoleA() : scenario.getRoleB())
                .assignedRoleB(keepScenarioRoles ? scenario.getRoleB() : scenario.getRoleA())
                .status(SessionStatus.PREPARATION)
                .currentPhase(SessionStatus.PREPARATION)
                .preparationSeconds(AppConstants.ROLEPLAY_PREPARATION_SECONDS)
                .sessionDurationSeconds(AppConstants.ROLEPLAY_SESSION_DURATION_SECONDS)
                .startTime(now)
                .endTime(now.plusSeconds(AppConstants.ROLEPLAY_PREPARATION_SECONDS))
                .build();

        return mapToResponse(roleplaySessionRepository.save(roleplaySession));
    }

    @Transactional(readOnly = true)
    public RoleplaySessionResponse getSession(Long sessionId) {
        return mapToResponse(loadSession(sessionId));
    }

    @Transactional
    public RoleplaySessionResponse startPreparation(Long sessionId) {
        RoleplaySession session = loadSession(sessionId);

        if (session.getStatus() == SessionStatus.ACTIVE || session.getStatus() == SessionStatus.COMPLETED) {
            throw new BadRequestException("Cannot start preparation for active or completed roleplay");
        }

        LocalDateTime now = LocalDateTime.now();
        session.setStatus(SessionStatus.PREPARATION);
        session.setCurrentPhase(SessionStatus.PREPARATION);
        session.setStartTime(now);
        session.setEndTime(now.plusSeconds(session.getPreparationSeconds()));

        return mapToResponse(roleplaySessionRepository.save(session));
    }

    @Transactional
    public RoleplaySessionResponse startRoleplay(Long sessionId) {
        RoleplaySession session = loadSession(sessionId);
        validateStatus(session, SessionStatus.PREPARATION, "Cannot start roleplay unless status is PREPARATION");

        LocalDateTime now = LocalDateTime.now();
        session.setStatus(SessionStatus.ACTIVE);
        session.setCurrentPhase(SessionStatus.ACTIVE);
        session.setStartTime(now);
        session.setEndTime(now.plusSeconds(session.getSessionDurationSeconds()));

        return mapToResponse(roleplaySessionRepository.save(session));
    }

    @Transactional
    public RoleplaySessionResponse completeRoleplay(Long sessionId) {
        throw new BadRequestException("Submit required feedback to complete roleplay session");
    }

    @Transactional(readOnly = true)
    public RoleplaySession loadSession(Long sessionId) {
        return roleplaySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Roleplay session not found"));
    }

    public RoleplaySessionResponse mapToResponse(RoleplaySession session) {
        return RoleplaySessionResponse.builder()
                .sessionId(session.getId())
                .scenarioId(session.getScenario().getId())
                .scenarioTitle(session.getScenario().getTitle())
                .scenarioDescription(session.getScenario().getDescription())
                .participantAId(session.getParticipantA().getId())
                .participantAName(session.getParticipantA().getName())
                .participantBId(session.getParticipantB().getId())
                .participantBName(session.getParticipantB().getName())
                .assignedRoleA(session.getAssignedRoleA())
                .assignedRoleB(session.getAssignedRoleB())
                .status(session.getStatus())
                .currentPhase(session.getCurrentPhase())
                .preparationSeconds(session.getPreparationSeconds())
                .sessionDurationSeconds(session.getSessionDurationSeconds())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .build();
    }

    private RoleplayScenario selectRandomScenario() {
        List<RoleplayScenario> scenarios = roleplayScenarioRepository.findByActiveTrue();

        if (scenarios.isEmpty()) {
            throw new BadRequestException("No active roleplay scenarios available");
        }

        int index = ThreadLocalRandom.current().nextInt(scenarios.size());
        return scenarios.get(index);
    }

    private void validateStatus(
            RoleplaySession session,
            SessionStatus expectedStatus,
            String message
    ) {
        if (session.getStatus() != expectedStatus) {
            throw new BadRequestException(message);
        }
    }
}
