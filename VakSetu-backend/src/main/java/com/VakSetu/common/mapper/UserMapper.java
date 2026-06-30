package com.vaksetu.common.mapper;

import com.vaksetu.user.dto.UserProfileResponse;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserProfileResponse toProfileResponse(User user, UserSkill userSkill) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .overallScore(user.getOverallScore())
                .reputation(user.getReputation())
                .rank(user.getRank())
                .contributorBadge(user.getContributorBadge())
                .fluency(userSkill.getFluency())
                .pronunciation(userSkill.getPronunciation())
                .grammar(userSkill.getGrammar())
                .confidence(userSkill.getConfidence())
                .empathy(userSkill.getEmpathy())
                .listening(userSkill.getListening())
                .engagement(userSkill.getEngagement())
                .build();
    }
}
