package com.vaksetu.statistics.service;

import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserRepository userRepository;

    @Transactional
    public void incrementGdSessionsJoined(User user) {
        int joined = user.getGdSessionsJoined() == null ? 0 : user.getGdSessionsJoined();
        user.setGdSessionsJoined(joined + 1);
        userRepository.save(user);
    }

    @Transactional
    public void incrementDebateCompleted(User user) {
        incrementSessionsCompleted(user);

        int debatesCompleted = user.getDebatesCompleted() == null ? 0 : user.getDebatesCompleted();
        user.setDebatesCompleted(debatesCompleted + 1);
        userRepository.save(user);
    }

    @Transactional
    public void incrementRoleplayCompleted(User user) {
        incrementSessionsCompleted(user);

        int roleplaysCompleted = user.getRoleplaysCompleted() == null ? 0 : user.getRoleplaysCompleted();
        user.setRoleplaysCompleted(roleplaysCompleted + 1);
        userRepository.save(user);
    }

    @Transactional
    public void updateHighestSessionStars(
            User user,
            int starsReceived
    ) {
        int highestSessionStars = user.getHighestSessionStars() == null ? 0 : user.getHighestSessionStars();

        if (starsReceived > highestSessionStars) {
            user.setHighestSessionStars(starsReceived);
        }

        userRepository.save(user);
    }

    @Transactional
    public void incrementTopContributorFinishes(User user) {
        int finishes = user.getTopContributorFinishes() == null ? 0 : user.getTopContributorFinishes();
        user.setTopContributorFinishes(finishes + 1);
        userRepository.save(user);
    }

    private void incrementSessionsCompleted(User user) {
        int sessionsCompleted = user.getSessionsCompleted() == null ? 0 : user.getSessionsCompleted();
        user.setSessionsCompleted(sessionsCompleted + 1);
    }
}
