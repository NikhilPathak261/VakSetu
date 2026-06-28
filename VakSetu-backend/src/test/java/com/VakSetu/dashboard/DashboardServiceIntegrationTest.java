package com.vaksetu.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaksetu.common.enums.Rank;
import com.vaksetu.common.enums.Role;
import com.vaksetu.common.enums.SessionType;
import com.vaksetu.dashboard.dto.DashboardSummaryResponse;
import com.vaksetu.dashboard.dto.ReputationHistoryResponse;
import com.vaksetu.dashboard.dto.SkillHistoryResponse;
import com.vaksetu.dashboard.service.DashboardService;
import com.vaksetu.reputation.entity.ReputationHistory;
import com.vaksetu.reputation.repository.ReputationHistoryRepository;
import com.vaksetu.skill.entity.SkillHistory;
import com.vaksetu.skill.repository.SkillHistoryRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.user.repository.UserSkillRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DashboardServiceIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillHistoryRepository skillHistoryRepository;

    @Autowired
    private ReputationHistoryRepository reputationHistoryRepository;

    @Test
    void getSummaryReturnsDashboardDtoWithoutEntityExposure() {
        User user = createUser();
        createUserSkill(user);

        DashboardSummaryResponse response = dashboardService.getSummary(user.getId());

        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getName()).isEqualTo("Dashboard User");
        assertThat(response.getRank()).isEqualTo(Rank.COMMUNICATOR);
        assertThat(response.getSkills().getFluency()).isEqualTo(61.0);
        assertThat(response.getStatistics().getSessionsCompleted()).isEqualTo(3);
        assertThat(response.getStatistics().getGdSessionsJoined()).isEqualTo(1);
    }

    @Test
    void historiesReturnRecentEntriesForAuthenticatedUserOnly() {
        User user = createUser();
        createUserSkill(user);
        User otherUser = createUser("other-dashboard-user@example.com");
        createUserSkill(otherUser);

        SkillHistory olderSkillHistory = skillHistoryRepository.save(SkillHistory.builder()
                .user(user)
                .skillName("fluency")
                .oldValue(50.0)
                .newValue(55.0)
                .sessionId(1L)
                .sessionType(SessionType.DEBATE)
                .build());
        SkillHistory newerSkillHistory = skillHistoryRepository.save(SkillHistory.builder()
                .user(user)
                .skillName("confidence")
                .oldValue(55.0)
                .newValue(60.0)
                .sessionId(2L)
                .sessionType(SessionType.ROLEPLAY)
                .build());
        skillHistoryRepository.save(SkillHistory.builder()
                .user(otherUser)
                .skillName("grammar")
                .oldValue(40.0)
                .newValue(45.0)
                .sessionId(3L)
                .sessionType(SessionType.DEBATE)
                .build());

        ReputationHistory reputationHistory = reputationHistoryRepository.save(ReputationHistory.builder()
                .user(user)
                .changeAmount(2)
                .reason("Completed session")
                .build());
        reputationHistoryRepository.save(ReputationHistory.builder()
                .user(otherUser)
                .changeAmount(2)
                .reason("Other user completed session")
                .build());

        List<SkillHistoryResponse> skillHistory = dashboardService.getSkillHistory(user.getId());
        List<ReputationHistoryResponse> reputationHistoryResponses =
                dashboardService.getReputationHistory(user.getId());

        assertThat(skillHistory).extracting(SkillHistoryResponse::getId)
                .containsExactly(newerSkillHistory.getId(), olderSkillHistory.getId());
        assertThat(reputationHistoryResponses).singleElement()
                .extracting(ReputationHistoryResponse::getId)
                .isEqualTo(reputationHistory.getId());
    }

    private User createUser() {
        return createUser("dashboard-user@example.com");
    }

    private User createUser(String email) {
        return userRepository.save(User.builder()
                .name("Dashboard User")
                .email(email)
                .passwordHash("hashed-password")
                .overallScore(62.0)
                .reputation(54)
                .rank(Rank.COMMUNICATOR)
                .totalStars(5)
                .topContributorFinishes(1)
                .highestSessionStars(3)
                .sessionsCompleted(3)
                .debatesCompleted(2)
                .roleplaysCompleted(1)
                .gdSessionsJoined(1)
                .role(Role.USER)
                .build());
    }

    private void createUserSkill(User user) {
        userSkillRepository.save(UserSkill.builder()
                .user(user)
                .fluency(61.0)
                .pronunciation(62.0)
                .grammar(63.0)
                .confidence(64.0)
                .empathy(65.0)
                .listening(66.0)
                .engagement(67.0)
                .build());
    }
}
