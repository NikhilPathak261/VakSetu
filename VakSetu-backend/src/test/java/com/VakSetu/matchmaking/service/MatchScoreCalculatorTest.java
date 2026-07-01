package com.vaksetu.matchmaking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaksetu.matchmaking.dto.UserSkillSnapshot;
import org.junit.jupiter.api.Test;

class MatchScoreCalculatorTest {

    private final MatchScoreCalculator calculator = new MatchScoreCalculator();

    @Test
    void calculatesScoreWithNullAndOutOfRangeSnapshotValuesSafely() {
        UserSkillSnapshot first = UserSkillSnapshot.builder()
                .overallScore(null)
                .fluency(-20.0)
                .pronunciation(120.0)
                .grammar(null)
                .confidence(50.0)
                .empathy(50.0)
                .listening(50.0)
                .engagement(50.0)
                .build();
        UserSkillSnapshot second = UserSkillSnapshot.builder()
                .overallScore(50.0)
                .fluency(100.0)
                .pronunciation(0.0)
                .grammar(50.0)
                .confidence(50.0)
                .empathy(50.0)
                .listening(50.0)
                .engagement(50.0)
                .build();

        double score = calculator.calculateScore(first, second);

        assertThat(score).isFinite();
        assertThat(score).isBetween(100.0, 115.0);
    }
}
