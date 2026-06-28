package com.vaksetu.skill.service;

import com.vaksetu.common.constants.AppConstants;
import com.vaksetu.common.enums.SessionType;
import com.vaksetu.common.util.RankCalculationUtil;
import com.vaksetu.feedback.dto.SkillRatingScores;
import com.vaksetu.skill.entity.SkillHistory;
import com.vaksetu.skill.repository.SkillHistoryRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.user.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillHistoryRepository skillHistoryRepository;

    @Transactional
    public void updateSkills(
            User user,
            Long sessionId,
            SessionType sessionType,
            SkillRatingScores sessionRatings
    ) {
        UserSkill userSkill = userSkillRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("User skill not found"));

        userSkill.setFluency(updateSkill(user, "fluency", userSkill.getFluency(), sessionRatings.getFluency(), sessionId, sessionType));
        userSkill.setPronunciation(updateSkill(user, "pronunciation", userSkill.getPronunciation(), sessionRatings.getPronunciation(), sessionId, sessionType));
        userSkill.setGrammar(updateSkill(user, "grammar", userSkill.getGrammar(), sessionRatings.getGrammar(), sessionId, sessionType));
        userSkill.setConfidence(updateSkill(user, "confidence", userSkill.getConfidence(), sessionRatings.getConfidence(), sessionId, sessionType));
        userSkill.setEmpathy(updateSkill(user, "empathy", userSkill.getEmpathy(), sessionRatings.getEmpathy(), sessionId, sessionType));
        userSkill.setListening(updateSkill(user, "listening", userSkill.getListening(), sessionRatings.getListening(), sessionId, sessionType));
        userSkill.setEngagement(updateSkill(user, "engagement", userSkill.getEngagement(), sessionRatings.getEngagement(), sessionId, sessionType));

        userSkillRepository.save(userSkill);

        double overallScore = (
                userSkill.getFluency()
                        + userSkill.getPronunciation()
                        + userSkill.getGrammar()
                        + userSkill.getConfidence()
                        + userSkill.getEmpathy()
                        + userSkill.getListening()
                        + userSkill.getEngagement()
        ) / 7.0;

        user.setOverallScore(clampSkill(overallScore));
        user.setRank(RankCalculationUtil.calculateRank(user.getOverallScore()));
        userRepository.save(user);
    }

    private Double updateSkill(
            User user,
            String skillName,
            Double oldValue,
            Double sessionRating,
            Long sessionId,
            SessionType sessionType
    ) {
        double newValue = clampSkill(
                (AppConstants.SKILL_HISTORY_WEIGHT * oldValue)
                        + (AppConstants.SESSION_RATING_WEIGHT * sessionRating)
        );

        skillHistoryRepository.save(SkillHistory.builder()
                .user(user)
                .skillName(skillName)
                .oldValue(oldValue)
                .newValue(newValue)
                .sessionId(sessionId)
                .sessionType(sessionType)
                .build());

        return newValue;
    }

    private double clampSkill(double value) {
        return Math.max(
                AppConstants.MIN_SKILL_SCORE,
                Math.min(AppConstants.MAX_SKILL_SCORE, value)
        );
    }
}
