package com.vaksetu.user.service;

import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.user.dto.UpdateProfileRequest;
import com.vaksetu.user.dto.UserProfileResponse;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.user.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserSkill userSkill = userSkillRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));

        return buildUserProfileResponse(user, userSkill);
    }

    @Transactional
    public UserProfileResponse updateProfile(
            Long userId,
            UpdateProfileRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(request.getName());

        UserSkill userSkill = userSkillRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));

        return buildUserProfileResponse(user, userSkill);
    }

    private UserProfileResponse buildUserProfileResponse(User user, UserSkill userSkill) {
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
