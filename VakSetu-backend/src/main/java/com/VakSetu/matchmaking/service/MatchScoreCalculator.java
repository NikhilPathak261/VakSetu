package com.vaksetu.matchmaking.service;

import com.vaksetu.matchmaking.dto.UserSkillSnapshot;
import org.springframework.stereotype.Service;

@Service
public class MatchScoreCalculator {

    public double calculateScore(
            UserSkillSnapshot first,
            UserSkillSnapshot second
    ) {
        double totalScore = 0.0;

        totalScore += calculateFieldScore(first.getOverallScore(), second.getOverallScore());
        totalScore += calculateFieldScore(first.getFluency(), second.getFluency());
        totalScore += calculateFieldScore(first.getPronunciation(), second.getPronunciation());
        totalScore += calculateFieldScore(first.getGrammar(), second.getGrammar());
        totalScore += calculateFieldScore(first.getConfidence(), second.getConfidence());
        totalScore += calculateFieldScore(first.getEmpathy(), second.getEmpathy());
        totalScore += calculateFieldScore(first.getListening(), second.getListening());
        totalScore += calculateFieldScore(first.getEngagement(), second.getEngagement());

        return totalScore / 8;
    }

    private double calculateFieldScore(Double firstValue, Double secondValue) {
        return 100 - Math.abs(firstValue - secondValue);
    }
}
