package com.vaksetu.gd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vaksetu.common.enums.Rank;
import com.vaksetu.common.enums.Role;
import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.exception.BadRequestException;
import com.vaksetu.gd.dto.CreateGDSessionRequest;
import com.vaksetu.gd.dto.GDSessionResponse;
import com.vaksetu.gd.dto.GiveStarRequest;
import com.vaksetu.gd.service.GDSessionService;
import com.vaksetu.gd.service.GDStarService;
import com.vaksetu.reputation.repository.ReputationHistoryRepository;
import com.vaksetu.skill.repository.SkillHistoryRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.user.repository.UserSkillRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GDServiceIntegrationTest {

    @Autowired
    private GDSessionService gdSessionService;

    @Autowired
    private GDStarService gdStarService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillHistoryRepository skillHistoryRepository;

    @Autowired
    private ReputationHistoryRepository reputationHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createJoinLeaveUpdatesGdParticipationWithoutChangingSkillsOrReputation() {
        User creator = saveUser("GD Creator", "gd.creator@example.com");
        User participant = saveUser("GD Participant", "gd.participant@example.com");
        saveUserSkill(creator);
        saveUserSkill(participant);

        GDSessionResponse createdRoom = gdSessionService.createRoom(
                creator.getId(),
                CreateGDSessionRequest.builder()
                        .topic("Public speaking practice")
                        .maxParticipants(3)
                        .build()
        );

        gdSessionService.joinRoom(participant.getId(), createdRoom.getSessionId());
        gdSessionService.leaveRoom(participant.getId(), createdRoom.getSessionId());

        entityManager.flush();
        entityManager.clear();

        User updatedCreator = userRepository.findById(creator.getId()).orElseThrow();
        User updatedParticipant = userRepository.findById(participant.getId()).orElseThrow();
        UserSkill creatorSkill = userSkillRepository.findByUserId(creator.getId()).orElseThrow();
        UserSkill participantSkill = userSkillRepository.findByUserId(participant.getId()).orElseThrow();
        GDSessionResponse room = gdSessionService.getRoom(createdRoom.getSessionId());

        assertThat(room.getCurrentParticipants()).isEqualTo(1);
        assertThat(room.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(updatedCreator.getGdSessionsJoined()).isEqualTo(1);
        assertThat(updatedParticipant.getGdSessionsJoined()).isEqualTo(1);

        assertCoreScoreUnchanged(updatedCreator, creatorSkill);
        assertCoreScoreUnchanged(updatedParticipant, participantSkill);
        assertThat(skillHistoryRepository.findByUserId(creator.getId())).isEmpty();
        assertThat(skillHistoryRepository.findByUserId(participant.getId())).isEmpty();
        assertThat(reputationHistoryRepository.findByUserId(creator.getId())).isEmpty();
        assertThat(reputationHistoryRepository.findByUserId(participant.getId())).isEmpty();
    }

    @Test
    void starRequiresReceiverToSpeakAndPreventsDuplicateOrSelfStars() {
        User giver = saveUser("GD Giver", "gd.giver@example.com");
        User receiver = saveUser("GD Receiver", "gd.receiver@example.com");
        saveUserSkill(giver);
        saveUserSkill(receiver);

        GDSessionResponse room = gdSessionService.createRoom(
                giver.getId(),
                CreateGDSessionRequest.builder()
                        .topic("Leadership practice")
                        .maxParticipants(4)
                        .build()
        );
        gdSessionService.joinRoom(receiver.getId(), room.getSessionId());

        GiveStarRequest starRequest = GiveStarRequest.builder()
                .sessionId(room.getSessionId())
                .receiverId(receiver.getId())
                .build();

        assertThatThrownBy(() -> gdStarService.giveStar(giver.getId(), starRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Receiver has not spoken yet");

        gdSessionService.markSpoken(receiver.getId(), room.getSessionId());
        gdStarService.giveStar(giver.getId(), starRequest);

        assertThatThrownBy(() -> gdStarService.giveStar(giver.getId(), starRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Star already given");

        assertThatThrownBy(() -> gdStarService.giveStar(receiver.getId(), GiveStarRequest.builder()
                .sessionId(room.getSessionId())
                .receiverId(receiver.getId())
                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot give star to yourself");

        entityManager.flush();
        entityManager.clear();

        User updatedReceiver = userRepository.findById(receiver.getId()).orElseThrow();
        UserSkill updatedReceiverSkill = userSkillRepository.findByUserId(receiver.getId()).orElseThrow();

        assertThat(updatedReceiver.getTotalStars()).isEqualTo(1);
        assertCoreScoreUnchanged(updatedReceiver, updatedReceiverSkill);
        assertThat(skillHistoryRepository.findByUserId(receiver.getId())).isEmpty();
        assertThat(reputationHistoryRepository.findByUserId(receiver.getId())).isEmpty();
    }

    @Test
    void closingGdRoomUpdatesBadgesAndHighestStarsWithoutChangingSkillOrReputation() {
        User creator = saveUser("GD Close Creator", "gd.close.creator@example.com");
        User receiver = saveUser("GD Close Receiver", "gd.close.receiver@example.com");
        saveUserSkill(creator);
        saveUserSkill(receiver);

        GDSessionResponse room = gdSessionService.createRoom(
                creator.getId(),
                CreateGDSessionRequest.builder()
                        .topic("Closing practice")
                        .maxParticipants(4)
                        .build()
        );
        gdSessionService.joinRoom(receiver.getId(), room.getSessionId());
        gdSessionService.markSpoken(receiver.getId(), room.getSessionId());
        gdStarService.giveStar(creator.getId(), GiveStarRequest.builder()
                .sessionId(room.getSessionId())
                .receiverId(receiver.getId())
                .build());

        GDSessionResponse closedRoom = gdSessionService.closeRoom(creator.getId(), room.getSessionId());

        entityManager.flush();
        entityManager.clear();

        User updatedReceiver = userRepository.findById(receiver.getId()).orElseThrow();
        UserSkill updatedReceiverSkill = userSkillRepository.findByUserId(receiver.getId()).orElseThrow();

        assertThat(closedRoom.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(updatedReceiver.getHighestSessionStars()).isEqualTo(1);
        assertThat(updatedReceiver.getTopContributorFinishes()).isEqualTo(1);
        assertThat(updatedReceiver.getContributorBadge()).isNotNull();
        assertCoreScoreUnchanged(updatedReceiver, updatedReceiverSkill);
        assertThat(skillHistoryRepository.findByUserId(receiver.getId())).isEmpty();
        assertThat(reputationHistoryRepository.findByUserId(receiver.getId())).isEmpty();
    }

    private void assertCoreScoreUnchanged(
            User user,
            UserSkill userSkill
    ) {
        assertThat(user.getOverallScore()).isEqualTo(50.0);
        assertThat(user.getReputation()).isEqualTo(50);
        assertThat(user.getRank()).isEqualTo(Rank.CONVERSATIONALIST);
        assertThat(userSkill.getFluency()).isEqualTo(50.0);
        assertThat(userSkill.getPronunciation()).isEqualTo(50.0);
        assertThat(userSkill.getGrammar()).isEqualTo(50.0);
        assertThat(userSkill.getConfidence()).isEqualTo(50.0);
        assertThat(userSkill.getEmpathy()).isEqualTo(50.0);
        assertThat(userSkill.getListening()).isEqualTo(50.0);
        assertThat(userSkill.getEngagement()).isEqualTo(50.0);
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
