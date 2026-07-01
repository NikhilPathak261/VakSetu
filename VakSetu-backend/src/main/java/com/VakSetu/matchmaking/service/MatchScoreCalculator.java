package com.vaksetu.matchmaking.service;

import com.vaksetu.common.constants.AppConstants;
import com.vaksetu.matchmaking.dto.UserSkillSnapshot;
import org.springframework.stereotype.Service;

@Service
public class MatchScoreCalculator {

    public double calculateScore(
            UserSkillSnapshot first,
            UserSkillSnapshot second
    ) {
        return calculateOverallCompatibilityScore(first, second)
                + calculateComplementarySkillScore(first, second);
    }

    private double calculateOverallCompatibilityScore(
            UserSkillSnapshot first,
            UserSkillSnapshot second
    ) {
        return calculateSimilarityScore(first.getOverallScore(), second.getOverallScore());
    }

    private double calculateComplementarySkillScore(
            UserSkillSnapshot first,
            UserSkillSnapshot second
    ) {
        double totalScore = 0.0;

        totalScore += calculateComplementScore(first.getFluency(), second.getFluency());
        totalScore += calculateComplementScore(first.getPronunciation(), second.getPronunciation());
        totalScore += calculateComplementScore(first.getGrammar(), second.getGrammar());
        totalScore += calculateComplementScore(first.getConfidence(), second.getConfidence());
        totalScore += calculateComplementScore(first.getEmpathy(), second.getEmpathy());
        totalScore += calculateComplementScore(first.getListening(), second.getListening());
        totalScore += calculateComplementScore(first.getEngagement(), second.getEngagement());

        return totalScore / 7;
    }

    private double calculateSimilarityScore(Double firstValue, Double secondValue) {
        return AppConstants.MAX_SKILL_SCORE - Math.abs(normalize(firstValue) - normalize(secondValue));
    }

    private double calculateComplementScore(Double firstValue, Double secondValue) {
        double difference = Math.abs(normalize(firstValue) - normalize(secondValue));

        return difference * 0.5;
    }

    private double normalize(Double value) {
        if (value == null) {
            return AppConstants.INITIAL_SKILL_SCORE.doubleValue();
        }

        return Math.max(
                AppConstants.MIN_SKILL_SCORE,
                Math.min(AppConstants.MAX_SKILL_SCORE, value)
        );
    }
}
