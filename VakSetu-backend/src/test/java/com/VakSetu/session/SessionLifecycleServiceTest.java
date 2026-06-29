package com.vaksetu.session;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.vaksetu.common.enums.DebateSide;
import com.vaksetu.common.enums.DifficultyLevel;
import com.vaksetu.common.enums.Rank;
import com.vaksetu.common.enums.Role;
import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.debate.entity.DebateSession;
import com.vaksetu.debate.repository.DebateSessionRepository;
import com.vaksetu.debate.service.DebateRuntimeService;
import com.vaksetu.exception.BadRequestException;
import com.vaksetu.roleplay.entity.RoleplayScenario;
import com.vaksetu.roleplay.entity.RoleplaySession;
import com.vaksetu.roleplay.repository.RoleplayScenarioRepository;
import com.vaksetu.roleplay.repository.RoleplaySessionRepository;
import com.vaksetu.roleplay.service.RoleplaySessionService;
import com.vaksetu.topic.entity.Topic;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SessionLifecycleServiceTest {

    private final DebateSessionRepository debateSessionRepository =
            org.mockito.Mockito.mock(DebateSessionRepository.class);
    private final RoleplaySessionRepository roleplaySessionRepository =
            org.mockito.Mockito.mock(RoleplaySessionRepository.class);
    private final RoleplayScenarioRepository roleplayScenarioRepository =
            org.mockito.Mockito.mock(RoleplayScenarioRepository.class);
    private final UserRepository userRepository =
            org.mockito.Mockito.mock(UserRepository.class);

    private final DebateRuntimeService debateRuntimeService = new DebateRuntimeService(debateSessionRepository);
    private final RoleplaySessionService roleplaySessionService = new RoleplaySessionService(
            roleplaySessionRepository,
            roleplayScenarioRepository,
            userRepository
    );

    @Test
    void startRoundOneRejectsBeforePreparationEnds() {
        DebateSession session = createDebateSession(SessionStatus.PREPARATION, LocalDateTime.now().plusMinutes(1));
        when(debateSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> debateRuntimeService.startRoundOne(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot start ROUND_1 before preparation ends");
    }

    @Test
    void startRoundTwoRejectsBeforeRoundOneEnds() {
        DebateSession session = createDebateSession(SessionStatus.ROUND_1, LocalDateTime.now().plusMinutes(1));
        when(debateSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> debateRuntimeService.startRoundTwo(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot start ROUND_2 before ROUND_1 ends");
    }

    @Test
    void startRoleplayRejectsBeforePreparationEnds() {
        RoleplaySession session = createRoleplaySession(LocalDateTime.now().plusMinutes(1));
        when(roleplaySessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> roleplaySessionService.startRoleplay(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot start roleplay before preparation ends");

        verifyNoInteractions(roleplayScenarioRepository, userRepository);
    }

    private DebateSession createDebateSession(
            SessionStatus status,
            LocalDateTime roundEndTime
    ) {
        User participantA = createUser(10L);
        User participantB = createUser(20L);

        return DebateSession.builder()
                .id(1L)
                .topic(Topic.builder().id(1L).title("Topic").category("General").active(true).build())
                .participantA(participantA)
                .participantB(participantB)
                .sideA(DebateSide.FOR)
                .sideB(DebateSide.AGAINST)
                .status(status)
                .currentRound(status == SessionStatus.PREPARATION ? 0 : 1)
                .totalRounds(3)
                .currentSpeaker(status == SessionStatus.PREPARATION ? null : participantA)
                .preparationSeconds(120)
                .roundDurationSeconds(180)
                .roundStartTime(LocalDateTime.now().minusMinutes(1))
                .roundEndTime(roundEndTime)
                .build();
    }

    private RoleplaySession createRoleplaySession(LocalDateTime preparationEndTime) {
        return RoleplaySession.builder()
                .id(1L)
                .scenario(RoleplayScenario.builder()
                        .id(1L)
                        .title("Scenario")
                        .description("Description")
                        .roleA("Customer")
                        .roleB("Manager")
                        .difficulty(DifficultyLevel.EASY)
                        .active(true)
                        .build())
                .participantA(createUser(10L))
                .participantB(createUser(20L))
                .assignedRoleA("Customer")
                .assignedRoleB("Manager")
                .status(SessionStatus.PREPARATION)
                .currentPhase(SessionStatus.PREPARATION)
                .preparationSeconds(60)
                .sessionDurationSeconds(180)
                .startTime(LocalDateTime.now().minusMinutes(1))
                .endTime(preparationEndTime)
                .build();
    }

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .name("User " + id)
                .email("user" + id + "@example.com")
                .passwordHash("hashed")
                .overallScore(50.0)
                .reputation(50)
                .rank(Rank.CONVERSATIONALIST)
                .role(Role.USER)
                .totalStars(0)
                .topContributorFinishes(0)
                .highestSessionStars(0)
                .sessionsCompleted(0)
                .debatesCompleted(0)
                .roleplaysCompleted(0)
                .gdSessionsJoined(0)
                .build();
    }
}
