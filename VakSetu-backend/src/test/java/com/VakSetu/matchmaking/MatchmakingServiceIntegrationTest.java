package com.vaksetu.matchmaking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vaksetu.common.enums.DifficultyLevel;
import com.vaksetu.common.enums.Rank;
import com.vaksetu.common.enums.Role;
import com.vaksetu.common.enums.SessionType;
import com.vaksetu.debate.repository.DebateSessionRepository;
import com.vaksetu.exception.ConflictException;
import com.vaksetu.matchmaking.entity.MatchHistory;
import com.vaksetu.matchmaking.queue.DebateQueueEntry;
import com.vaksetu.matchmaking.queue.RoleplayQueueEntry;
import com.vaksetu.matchmaking.repository.MatchHistoryRepository;
import com.vaksetu.matchmaking.service.DebateQueueService;
import com.vaksetu.matchmaking.service.MatchmakingService;
import com.vaksetu.matchmaking.service.RoleplayQueueService;
import com.vaksetu.roleplay.entity.RoleplayScenario;
import com.vaksetu.roleplay.repository.RoleplayScenarioRepository;
import com.vaksetu.roleplay.repository.RoleplaySessionRepository;
import com.vaksetu.topic.entity.Topic;
import com.vaksetu.topic.repository.TopicRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.user.repository.UserSkillRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MatchmakingServiceIntegrationTest {

    @Autowired
    private MatchmakingService matchmakingService;

    @Autowired
    private DebateQueueService debateQueueService;

    @Autowired
    private RoleplayQueueService roleplayQueueService;

    @Autowired
    private MatchHistoryRepository matchHistoryRepository;

    @Autowired
    private DebateSessionRepository debateSessionRepository;

    @Autowired
    private RoleplaySessionRepository roleplaySessionRepository;

    @Autowired
    private RoleplayScenarioRepository roleplayScenarioRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void clearQueues() {
        debateQueueService.getAllEntries()
                .stream()
                .map(DebateQueueEntry::getUserId)
                .toList()
                .forEach(debateQueueService::removeUser);
        roleplayQueueService.getAllEntries()
                .stream()
                .map(RoleplayQueueEntry::getUserId)
                .toList()
                .forEach(roleplayQueueService::removeUser);
    }

    @Test
    void rejectsDuplicateQueueEntries() {
        User user = saveUser("Duplicate User", "duplicate.queue@example.com", 50.0);
        saveUserSkill(user, 50.0);
        Topic topic = saveTopic("Duplicate Topic");

        debateQueueService.addUser(user.getId(), topic.getId());

        assertThatThrownBy(() -> debateQueueService.addUser(user.getId(), topic.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User already in debate queue");

        roleplayQueueService.addUser(user.getId());

        assertThatThrownBy(() -> roleplayQueueService.addUser(user.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User already in roleplay queue");
    }

    @Test
    void debateMatchRequiresSameTopicAndCreatesSessionAndHistory() {
        User participantA = saveUser("Debate Match A", "debate.match.a@example.com", 50.0);
        User participantB = saveUser("Debate Match B", "debate.match.b@example.com", 54.0);
        saveUserSkill(participantA, 50.0);
        saveUserSkill(participantB, 60.0);
        Topic topic = saveTopic("Same Topic Match");

        debateQueueService.addUser(participantA.getId(), topic.getId());
        debateQueueService.addUser(participantB.getId(), topic.getId());

        Optional<DebateQueueEntry> match = matchmakingService.findDebateMatch(participantA.getId(), topic.getId());

        entityManager.flush();
        entityManager.clear();

        List<MatchHistory> history = matchHistoryRepository.findAll();

        assertThat(match).isPresent();
        assertThat(match.get().getUserId()).isEqualTo(participantB.getId());
        assertThat(debateQueueService.getAllEntries()).isEmpty();
        assertThat(debateSessionRepository.count()).isEqualTo(1);
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getSessionType()).isEqualTo(SessionType.DEBATE);
        assertThat(history.get(0).getMatchScore()).isPositive();
    }

    @Test
    void debateDoesNotMatchDifferentTopicOrIncompatibleOverallScore() {
        User participantA = saveUser("Debate No Match A", "debate.no.match.a@example.com", 50.0);
        User participantB = saveUser("Debate No Match B", "debate.no.match.b@example.com", 53.0);
        User participantC = saveUser("Debate No Match C", "debate.no.match.c@example.com", 90.0);
        saveUserSkill(participantA, 50.0);
        saveUserSkill(participantB, 53.0);
        saveUserSkill(participantC, 90.0);
        Topic topicA = saveTopic("Topic A");
        Topic topicB = saveTopic("Topic B");

        debateQueueService.addUser(participantA.getId(), topicA.getId());
        debateQueueService.addUser(participantB.getId(), topicB.getId());
        debateQueueService.addUser(participantC.getId(), topicA.getId());

        Optional<DebateQueueEntry> match = matchmakingService.findDebateMatch(participantA.getId(), topicA.getId());

        assertThat(match).isEmpty();
        assertThat(debateQueueService.getAllEntries()).hasSize(3);
        assertThat(debateSessionRepository.count()).isZero();
        assertThat(matchHistoryRepository.count()).isZero();
    }

    @Test
    void roleplayMatchCreatesScenarioBackedSessionAndHistory() {
        User participantA = saveUser("Roleplay Match A", "roleplay.match.a@example.com", 50.0);
        User participantB = saveUser("Roleplay Match B", "roleplay.match.b@example.com", 55.0);
        saveUserSkill(participantA, 50.0);
        saveUserSkill(participantB, 65.0);
        saveScenario();

        roleplayQueueService.addUser(participantA.getId());
        roleplayQueueService.addUser(participantB.getId());

        Optional<RoleplayQueueEntry> match = matchmakingService.findRoleplayMatch(participantA.getId());

        entityManager.flush();
        entityManager.clear();

        List<MatchHistory> history = matchHistoryRepository.findAll();

        assertThat(match).isPresent();
        assertThat(match.get().getUserId()).isEqualTo(participantB.getId());
        assertThat(roleplayQueueService.getAllEntries()).isEmpty();
        assertThat(roleplaySessionRepository.count()).isEqualTo(1);
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getSessionType()).isEqualTo(SessionType.ROLEPLAY);
        assertThat(history.get(0).getMatchScore()).isPositive();
    }

    private Topic saveTopic(String title) {
        return topicRepository.save(Topic.builder()
                .title(title)
                .category("Communication")
                .active(true)
                .build());
    }

    private RoleplayScenario saveScenario() {
        return roleplayScenarioRepository.save(RoleplayScenario.builder()
                .title("Customer Service")
                .description("Handle a support request")
                .roleA("Customer")
                .roleB("Agent")
                .difficulty(DifficultyLevel.EASY)
                .active(true)
                .build());
    }

    private User saveUser(
            String name,
            String email,
            Double overallScore
    ) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .passwordHash("encoded-password")
                .overallScore(overallScore)
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
                .build());
    }

    private void saveUserSkill(
            User user,
            Double skillValue
    ) {
        userSkillRepository.save(UserSkill.builder()
                .user(user)
                .fluency(skillValue)
                .pronunciation(skillValue)
                .grammar(skillValue)
                .confidence(skillValue)
                .empathy(skillValue)
                .listening(skillValue)
                .engagement(skillValue)
                .build());
    }
}
