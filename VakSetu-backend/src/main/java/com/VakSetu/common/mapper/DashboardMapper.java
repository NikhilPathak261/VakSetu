package com.vaksetu.common.mapper;

import com.vaksetu.dashboard.dto.DashboardSummaryResponse;
import com.vaksetu.dashboard.dto.ReputationHistoryResponse;
import com.vaksetu.dashboard.dto.SkillHistoryResponse;
import com.vaksetu.dashboard.dto.SkillSnapshotResponse;
import com.vaksetu.dashboard.dto.UserStatisticsResponse;
import com.vaksetu.reputation.entity.ReputationHistory;
import com.vaksetu.skill.entity.SkillHistory;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;

public final class DashboardMapper {

    private DashboardMapper() {
    }

    public static DashboardSummaryResponse toSummaryResponse(User user, UserSkill userSkill) {
        return DashboardSummaryResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .overallScore(user.getOverallScore())
                .reputation(user.getReputation())
                .rank(user.getRank())
                .contributorBadge(user.getContributorBadge())
                .skills(toSkillSnapshotResponse(userSkill))
                .statistics(toUserStatisticsResponse(user))
                .build();
    }

    public static SkillHistoryResponse toSkillHistoryResponse(SkillHistory skillHistory) {
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

    public static ReputationHistoryResponse toReputationHistoryResponse(
            ReputationHistory reputationHistory
    ) {
        return ReputationHistoryResponse.builder()
                .id(reputationHistory.getId())
                .changeAmount(reputationHistory.getChangeAmount())
                .reason(reputationHistory.getReason())
                .createdAt(reputationHistory.getCreatedAt())
                .build();
    }

    private static SkillSnapshotResponse toSkillSnapshotResponse(UserSkill userSkill) {
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

    private static UserStatisticsResponse toUserStatisticsResponse(User user) {
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
}
