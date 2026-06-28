package com.vaksetu.dashboard.service;

import com.vaksetu.dashboard.dto.DashboardSummaryResponse;
import com.vaksetu.dashboard.dto.ReputationHistoryResponse;
import com.vaksetu.dashboard.dto.SkillHistoryResponse;
import com.vaksetu.dashboard.dto.SkillSnapshotResponse;
import com.vaksetu.dashboard.dto.UserStatisticsResponse;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.reputation.entity.ReputationHistory;
import com.vaksetu.reputation.repository.ReputationHistoryRepository;
import com.vaksetu.skill.entity.SkillHistory;
import com.vaksetu.skill.repository.SkillHistoryRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.user.repository.UserSkillRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillHistoryRepository skillHistoryRepository;
    private final ReputationHistoryRepository reputationHistoryRepository;

    public DashboardSummaryResponse getSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserSkill userSkill = userSkillRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));

        return DashboardSummaryResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .overallScore(user.getOverallScore())
                .reputation(user.getReputation())
                .rank(user.getRank())
                .contributorBadge(user.getContributorBadge())
                .skills(buildSkillSnapshot(userSkill))
                .statistics(buildUserStatistics(user))
                .build();
    }

    public List<SkillHistoryResponse> getSkillHistory(Long userId) {
        ensureUserExists(userId);

        return skillHistoryRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::buildSkillHistoryResponse)
                .toList();
    }

    public List<ReputationHistoryResponse> getReputationHistory(Long userId) {
        ensureUserExists(userId);

        return reputationHistoryRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::buildReputationHistoryResponse)
                .toList();
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private SkillSnapshotResponse buildSkillSnapshot(UserSkill userSkill) {
        return SkillSnapshotResponse.builder()
                .fluency(userSkill.getFluency())
                .pronunciation(userSkill.getPronunciation())
                .grammar(userSkill.getGrammar())
                .confidence(userSkill.getConfidence())
                .empathy(userSkill.getEmpathy())
                .listening(userSkill.getListening())
                .engagement(userSkill.getEngagement())
                .build();
    }

    private UserStatisticsResponse buildUserStatistics(User user) {
        return UserStatisticsResponse.builder()
                .totalStars(user.getTotalStars())
                .highestSessionStars(user.getHighestSessionStars())
                .topContributorFinishes(user.getTopContributorFinishes())
                .sessionsCompleted(user.getSessionsCompleted())
                .debatesCompleted(user.getDebatesCompleted())
                .roleplaysCompleted(user.getRoleplaysCompleted())
                .gdSessionsJoined(user.getGdSessionsJoined())
                .build();
    }

    private SkillHistoryResponse buildSkillHistoryResponse(SkillHistory skillHistory) {
        return SkillHistoryResponse.builder()
                .id(skillHistory.getId())
                .skillName(skillHistory.getSkillName())
                .oldValue(skillHistory.getOldValue())
                .newValue(skillHistory.getNewValue())
                .sessionId(skillHistory.getSessionId())
                .sessionType(skillHistory.getSessionType())
                .createdAt(skillHistory.getCreatedAt())
                .build();
    }

    private ReputationHistoryResponse buildReputationHistoryResponse(
            ReputationHistory reputationHistory
    ) {
        return ReputationHistoryResponse.builder()
                .id(reputationHistory.getId())
                .changeAmount(reputationHistory.getChangeAmount())
                .reason(reputationHistory.getReason())
                .createdAt(reputationHistory.getCreatedAt())
                .build();
    }
}
