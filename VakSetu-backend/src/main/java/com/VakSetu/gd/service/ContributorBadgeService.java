package com.vaksetu.gd.service;

import com.vaksetu.common.enums.BadgeType;
import com.vaksetu.common.util.ContributorBadgeUtil;
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
        BadgeType badgeType = ContributorBadgeUtil.calculateBadge(user.getTopContributorFinishes());

        if (user.getContributorBadge() != badgeType) {
            user.setContributorBadge(badgeType);
            userRepository.save(user);
        }
    }
}
