package com.vaksetu.feedback.service;

import com.vaksetu.feedback.dto.SkillRatingScores;
import com.vaksetu.feedback.entity.Feedback;
import com.vaksetu.user.entity.UserSkill;
import com.vaksetu.user.repository.UserSkillRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingCalculationService {

    private final UserSkillRepository userSkillRepository;

    public SkillRatingScores calculateSessionRatings(List<Feedback> feedbackList) {
        WeightedRating fluency = new WeightedRating();
        WeightedRating pronunciation = new WeightedRating();
        WeightedRating grammar = new WeightedRating();
        WeightedRating confidence = new WeightedRating();
        WeightedRating empathy = new WeightedRating();
        WeightedRating listening = new WeightedRating();
        WeightedRating engagement = new WeightedRating();

        for (Feedback feedback : feedbackList) {
            UserSkill evaluatorSkill = userSkillRepository.findByUserId(feedback.getEvaluator().getId())
                    .orElseThrow(() -> new IllegalStateException("Evaluator skill not found"));
            double evaluatorReputation = feedback.getEvaluator().getReputation() == null
                    ? 0.0
                    : feedback.getEvaluator().getReputation();

            fluency.add(feedback.getFluencyRating(), evaluatorSkill.getFluency(), evaluatorReputation);
            pronunciation.add(feedback.getPronunciationRating(), evaluatorSkill.getPronunciation(), evaluatorReputation);
            grammar.add(feedback.getGrammarRating(), evaluatorSkill.getGrammar(), evaluatorReputation);
            confidence.add(feedback.getConfidenceRating(), evaluatorSkill.getConfidence(), evaluatorReputation);
            empathy.add(feedback.getEmpathyRating(), evaluatorSkill.getEmpathy(), evaluatorReputation);
            listening.add(feedback.getListeningRating(), evaluatorSkill.getListening(), evaluatorReputation);
            engagement.add(feedback.getEngagementRating(), evaluatorSkill.getEngagement(), evaluatorReputation);
        }

        return SkillRatingScores.builder()
                .fluency(fluency.value())
                .pronunciation(pronunciation.value())
                .grammar(grammar.value())
                .confidence(confidence.value())
                .empathy(empathy.value())
                .listening(listening.value())
                .engagement(engagement.value())
                .build();
    }

    private static final class WeightedRating {

        private double weightedTotal;
        private double weightTotal;

        private void add(
                Integer rating,
                Double evaluatorRelevantSkill,
                double evaluatorReputation
        ) {
            double skill = evaluatorRelevantSkill == null ? 0.0 : evaluatorRelevantSkill;
            double weight = (skill / 100.0) * (evaluatorReputation / 100.0);
            double ratingPercent = (rating / 5.0) * 100.0;

            weightedTotal += ratingPercent * weight;
            weightTotal += weight;
        }

        private double value() {
            if (weightTotal == 0.0) {
                return 50.0;
            }

            return weightedTotal / weightTotal;
        }
    }
}
