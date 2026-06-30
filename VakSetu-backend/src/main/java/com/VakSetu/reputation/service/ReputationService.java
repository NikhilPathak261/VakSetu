package com.vaksetu.reputation.service;

import com.vaksetu.common.constants.AppConstants;
import com.vaksetu.reputation.entity.ReputationHistory;
import com.vaksetu.reputation.repository.ReputationHistoryRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReputationService {

    private final UserRepository userRepository;
    private final ReputationHistoryRepository reputationHistoryRepository;

    @Transactional
    public void changeReputation(
            User user,
            int changeAmount,
            String reason
    ) {
        int currentReputation = user.getReputation() == null ? 0 : user.getReputation();
        int newReputation = Math.max(
                AppConstants.MIN_REPUTATION,
                Math.min(AppConstants.MAX_REPUTATION, currentReputation + changeAmount)
        );
        int actualChangeAmount = newReputation - currentReputation;

        user.setReputation(newReputation);
        userRepository.save(user);

        reputationHistoryRepository.save(ReputationHistory.builder()
                .user(user)
                .changeAmount(actualChangeAmount)
                .reason(reason)
                .build());
    }

    public void rewardSessionCompletion(User user) {
        changeReputation(
                user,
                AppConstants.SESSION_REPUTATION_REWARD,
                "Session completed"
        );
    }
}
