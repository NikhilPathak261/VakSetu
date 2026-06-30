package com.vaksetu.reputation;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaksetu.common.enums.Rank;
import com.vaksetu.common.enums.Role;
import com.vaksetu.reputation.entity.ReputationHistory;
import com.vaksetu.reputation.repository.ReputationHistoryRepository;
import com.vaksetu.reputation.service.ReputationService;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReputationServiceIntegrationTest {

    @Autowired
    private ReputationService reputationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReputationHistoryRepository reputationHistoryRepository;

    @Test
    void changeReputationStoresActualClampedDeltaInHistory() {
        User user = userRepository.save(createUser(99));

        reputationService.rewardSessionCompletion(user);

        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        ReputationHistory history = reputationHistoryRepository.findByUserId(user.getId()).getFirst();

        assertThat(savedUser.getReputation()).isEqualTo(100);
        assertThat(history.getChangeAmount()).isEqualTo(1);
        assertThat(history.getReason()).isEqualTo("Session completed");
    }

    private User createUser(int reputation) {
        return User.builder()
                .name("Reputation User")
                .email("reputation.user@example.com")
                .passwordHash("hashed-password")
                .overallScore(50.0)
                .reputation(reputation)
                .rank(Rank.CONVERSATIONALIST)
                .totalStars(0)
                .topContributorFinishes(0)
                .highestSessionStars(0)
                .sessionsCompleted(0)
                .debatesCompleted(0)
                .roleplaysCompleted(0)
                .gdSessionsJoined(0)
                .role(Role.USER)
                .build();
    }
}
