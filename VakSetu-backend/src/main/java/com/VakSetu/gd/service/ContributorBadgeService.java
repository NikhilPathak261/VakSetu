package com.vaksetu.gd.service;

import com.vaksetu.common.enums.BadgeType;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContributorBadgeService {

    private final UserRepository userRepository;

    @Transactional
    public void updateBadge(User user) {
        BadgeType badgeType = calculateBadge(user.getTopContributorFinishes());

        if (user.getContributorBadge() != badgeType) {
            user.setContributorBadge(badgeType);
            userRepository.save(user);
        }
    }

    private BadgeType calculateBadge(Integer topContributorFinishes) {
        int finishes = topContributorFinishes == null ? 0 : topContributorFinishes;

        if (finishes >= 50) {
            return BadgeType.COMMUNITY_VOICE;
        }

        if (finishes >= 30) {
            return BadgeType.ELITE_CONTRIBUTOR;
        }

        if (finishes >= 15) {
            return BadgeType.SKILLED_CONTRIBUTOR;
        }

        if (finishes >= 5) {
            return BadgeType.RISING_CONTRIBUTOR;
        }

        return BadgeType.NONE;
    }
}
