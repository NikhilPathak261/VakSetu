package com.vaksetu.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vaksetu.common.enums.DebateSide;
import com.vaksetu.common.enums.DifficultyLevel;
import com.vaksetu.common.enums.Rank;
import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.common.enums.SessionType;
import com.vaksetu.debate.entity.DebateSession;
import com.vaksetu.debate.repository.DebateSessionRepository;
import com.vaksetu.feedback.dto.FeedbackResponse;
import com.vaksetu.feedback.dto.SubmitFeedbackRequest;
import com.vaksetu.feedback.service.FeedbackService;
import com.vaksetu.exception.BadRequestException;
import com.vaksetu.reputation.repository.ReputationHistoryRepository;
import com.vaksetu.roleplay.entity.RoleplayScenario;
import com.vaksetu.roleplay.entity.RoleplaySession;
import com.vaksetu.roleplay.repository.RoleplayScenarioRepository;
import com.vaksetu.roleplay.repository.RoleplaySessionRepository;
import com.vaksetu.skill.repository.SkillHistoryRepository;
import com.vaksetu.topic.entity.Topic;
import com.vaksetu.topic.repository.TopicRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.user.repository.UserSkillRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FeedbackCompletionIntegrationTest {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private DebateSessionRepository debateSessionRepository;

    @Autowired
    private RoleplayScenarioRepository roleplayScenarioRepository;

    @Autowired
    private RoleplaySessionRepository roleplaySessionRepository;

    @Autowired
    private SkillHistoryRepository skillHistoryRepository;

    @Autowired
    private ReputationHistoryRepository reputationHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void completesDebateAndUpdatesOwnedScoresAfterBothFeedbackSubmissions() {
        User participantA = saveUser("Participant A", "a@example.com");
        User participantB = saveUser("Participant B", "b@example.com");
        saveUserSkill(participantA);
        saveUserSkill(participantB);

        Topic topic = topicRepository.save(Topic.builder()
                .title("Public Speaking")
                .category("Communication")
                .active(true)
                .build());

        DebateSession session = debateSessionRepository.save(DebateSession.builder()
                .topic(topic)
                .participantA(participantA)
                .participantB(participantB)
                .sideA(DebateSide.FOR)
                .sideB(DebateSide.AGAINST)
                .status(SessionStatus.ROUND_3)
                .currentRound(3)
                .totalRounds(3)
                .preparationSeconds(120)
                .roundDurationSeconds(180)
                .roundStartTime(LocalDateTime.now().minusMinutes(4))
                .roundEndTime(LocalDateTime.now().minusMinutes(1))
                .build());

        FeedbackResponse firstResponse = feedbackService.submitFeedback(
                participantA.getId(),
                request(session.getId(), participantB.getId(), 5)
        );
        FeedbackResponse secondResponse = feedbackService.submitFeedback(
                participantB.getId(),
                request(session.getId(), participantA.getId(), 3)
        );

        entityManager.flush();
        entityManager.clear();

        User updatedA = userRepository.findById(participantA.getId()).orElseThrow();
        User updatedB = userRepository.findById(participantB.getId()).orElseThrow();
        UserSkill updatedASkill = userSkillRepository.findByUserId(participantA.getId()).orElseThrow();
        UserSkill updatedBSkill = userSkillRepository.findByUserId(participantB.getId()).orElseThrow();
        DebateSession completedSession = debateSessionRepository.findById(session.getId()).orElseThrow();

        assertThat(firstResponse.getSessionCompleted()).isFalse();
        assertThat(secondResponse.getSessionCompleted()).isTrue();
        assertThat(completedSession.getStatus()).isEqualTo(SessionStatus.COMPLETED);

        assertThat(updatedASkill.getFluency()).isEqualTo(53.0);
        assertThat(updatedBSkill.getFluency()).isEqualTo(65.0);
        assertThat(updatedA.getReputation()).isEqualTo(52);
        assertThat(updatedB.getReputation()).isEqualTo(52);
        assertThat(updatedA.getSessionsCompleted()).isEqualTo(1);
        assertThat(updatedB.getDebatesCompleted()).isEqualTo(1);
        assertThat(updatedA.getRank()).isEqualTo(Rank.CONVERSATIONALIST);

        assertThat(skillHistoryRepository.findByUserId(participantA.getId())).hasSize(7);
        assertThat(skillHistoryRepository.findByUserId(participantB.getId())).hasSize(7);
        assertThat(reputationHistoryRepository.findByUserId(participantA.getId())).hasSize(1);
        assertThat(reputationHistoryRepository.findByUserId(participantB.getId())).hasSize(1);
    }

    @Test
    void rejectsDebateFeedbackBeforeFinalRoundEnds() {
        User participantA = saveUser("Early Debate A", "early.debate.a@example.com");
        User participantB = saveUser("Early Debate B", "early.debate.b@example.com");
        saveUserSkill(participantA);
        saveUserSkill(participantB);

        Topic topic = topicRepository.save(Topic.builder()
                .title("Early Feedback")
                .category("Communication")
                .active(true)
                .build());

        DebateSession session = debateSessionRepository.save(DebateSession.builder()
                .topic(topic)
                .participantA(participantA)
                .participantB(participantB)
                .sideA(DebateSide.FOR)
                .sideB(DebateSide.AGAINST)
                .status(SessionStatus.ROUND_3)
                .currentRound(3)
                .totalRounds(3)
                .preparationSeconds(120)
                .roundDurationSeconds(180)
                .roundStartTime(LocalDateTime.now())
                .roundEndTime(LocalDateTime.now().plusMinutes(2))
                .build());

        assertThatThrownBy(() -> feedbackService.submitFeedback(
                participantA.getId(),
                request(session.getId(), participantB.getId(), 5)
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Debate feedback can be submitted after the final round ends");
    }

    @Test
    void rejectsRoleplayFeedbackBeforeActiveSessionEnds() {
        User participantA = saveUser("Early Roleplay A", "early.roleplay.a@example.com");
        User participantB = saveUser("Early Roleplay B", "early.roleplay.b@example.com");
        saveUserSkill(participantA);
        saveUserSkill(participantB);

        RoleplayScenario scenario = roleplayScenarioRepository.save(RoleplayScenario.builder()
                .title("Customer Support")
                .description("Handle a refund request")
                .roleA("Customer")
                .roleB("Manager")
                .difficulty(DifficultyLevel.EASY)
                .active(true)
                .build());

        RoleplaySession session = roleplaySessionRepository.save(RoleplaySession.builder()
                .scenario(scenario)
                .participantA(participantA)
                .participantB(participantB)
                .assignedRoleA("Customer")
                .assignedRoleB("Manager")
                .status(SessionStatus.ACTIVE)
                .currentPhase(SessionStatus.ACTIVE)
                .preparationSeconds(60)
                .sessionDurationSeconds(180)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(2))
                .build());

        assertThatThrownBy(() -> feedbackService.submitFeedback(
                participantA.getId(),
                roleplayRequest(session.getId(), participantB.getId(), 5)
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Roleplay feedback can be submitted after roleplay ends");
    }

    private SubmitFeedbackRequest request(
            Long sessionId,
            Long targetUserId,
            int rating
    ) {
        return SubmitFeedbackRequest.builder()
                .sessionId(sessionId)
                .sessionType(SessionType.DEBATE)
                .targetUserId(targetUserId)
                .fluencyRating(rating)
                .pronunciationRating(rating)
                .grammarRating(rating)
                .confidenceRating(rating)
                .empathyRating(rating)
                .listeningRating(rating)
                .engagementRating(rating)
                .build();
    }

    private SubmitFeedbackRequest roleplayRequest(
            Long sessionId,
            Long targetUserId,
            int rating
    ) {
        return SubmitFeedbackRequest.builder()
                .sessionId(sessionId)
                .sessionType(SessionType.ROLEPLAY)
                .targetUserId(targetUserId)
                .fluencyRating(rating)
                .pronunciationRating(rating)
                .grammarRating(rating)
                .confidenceRating(rating)
                .empathyRating(rating)
                .listeningRating(rating)
                .engagementRating(rating)
                .build();
    }

    private User saveUser(
            String name,
            String email
    ) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .passwordHash("encoded-password")
                .overallScore(50.0)
                .reputation(50)
                .rank(Rank.CONVERSATIONALIST)
                .role(com.vaksetu.common.enums.Role.USER)
                .totalStars(0)
                .topContributorFinishes(0)
                .highestSessionStars(0)
                .sessionsCompleted(0)
                .debatesCompleted(0)
                .roleplaysCompleted(0)
                .gdSessionsJoined(0)
                .build());
    }

    private void saveUserSkill(User user) {
        userSkillRepository.save(UserSkill.builder()
                .user(user)
                .fluency(50.0)
                .pronunciation(50.0)
                .grammar(50.0)
                .confidence(50.0)
                .empathy(50.0)
                .listening(50.0)
                .engagement(50.0)
                .build());
    }
}
