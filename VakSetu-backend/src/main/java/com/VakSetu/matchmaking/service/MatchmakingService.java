package com.vaksetu.matchmaking.service;

import com.vaksetu.common.constants.AppConstants;
import com.vaksetu.common.enums.SessionType;
import com.vaksetu.debate.dto.CreateDebateSessionRequest;
import com.vaksetu.debate.dto.DebateSessionResponse;
import com.vaksetu.debate.service.DebateSessionService;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.matchmaking.entity.MatchHistory;
import com.vaksetu.matchmaking.queue.DebateQueueEntry;
import com.vaksetu.matchmaking.queue.RoleplayQueueEntry;
import com.vaksetu.matchmaking.repository.MatchHistoryRepository;
import com.vaksetu.roleplay.dto.CreateRoleplaySessionRequest;
import com.vaksetu.roleplay.dto.RoleplaySessionResponse;
import com.vaksetu.roleplay.service.RoleplaySessionService;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.websocket.service.EventPublisherService;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final DebateQueueService debateQueueService;
    private final RoleplayQueueService roleplayQueueService;
    private final MatchHistoryRepository matchHistoryRepository;
    private final UserRepository userRepository;
    private final MatchScoreCalculator matchScoreCalculator;
    private final DebateSessionService debateSessionService;
    private final RoleplaySessionService roleplaySessionService;
    private final EventPublisherService eventPublisherService;
    private final ReentrantLock matchmakingLock = new ReentrantLock();

    @Transactional
    public Optional<DebateQueueEntry> findDebateMatch(
            Long userId,
            Long topicId
    ) {
        matchmakingLock.lock();

        try {
            DebateQueueEntry userEntry = findDebateQueueEntry(userId);
            Optional<DebateQueueEntry> candidate = debateQueueService.getAllEntries()
                    .stream()
                    .filter(entry -> !entry.getUserId().equals(userId))
                    .filter(entry -> entry.getTopicId().equals(topicId))
                    .filter(entry -> isOverallCompatible(userEntry, entry))
                    .max(Comparator.comparingDouble(entry -> matchScoreCalculator.calculateScore(
                            userEntry.getSkillSnapshot(),
                            entry.getSkillSnapshot()
                    )));

            if (candidate.isEmpty()) {
                return Optional.empty();
            }

            DebateQueueEntry candidateEntry = candidate.get();
            double matchScore = matchScoreCalculator.calculateScore(
                    userEntry.getSkillSnapshot(),
                    candidateEntry.getSkillSnapshot()
            );

            debateQueueService.removeMatchedUsers(userId, candidateEntry.getUserId());
            saveMatchHistory(userId, candidateEntry.getUserId(), SessionType.DEBATE, matchScore);
            DebateSessionResponse session = debateSessionService.createSession(CreateDebateSessionRequest.builder()
                    .topicId(topicId)
                    .participantAId(userId)
                    .participantBId(candidateEntry.getUserId())
                    .build());
            eventPublisherService.publishMatchFound(Map.of(
                    "sessionType", SessionType.DEBATE,
                    "sessionId", session.getId(),
                    "topicId", topicId,
                    "userAId", userId,
                    "userBId", candidateEntry.getUserId(),
                    "matchScore", matchScore
            ));

            return candidate;
        } finally {
            matchmakingLock.unlock();
        }
    }

    @Transactional
    public Optional<RoleplayQueueEntry> findRoleplayMatch(Long userId) {
        matchmakingLock.lock();

        try {
            RoleplayQueueEntry userEntry = findRoleplayQueueEntry(userId);
            Optional<RoleplayQueueEntry> candidate = roleplayQueueService.getAllEntries()
                    .stream()
                    .filter(entry -> !entry.getUserId().equals(userId))
                    .filter(entry -> isOverallCompatible(userEntry, entry))
                    .max(Comparator.comparingDouble(entry -> matchScoreCalculator.calculateScore(
                            userEntry.getSkillSnapshot(),
                            entry.getSkillSnapshot()
                    )));

            if (candidate.isEmpty()) {
                return Optional.empty();
            }

            RoleplayQueueEntry candidateEntry = candidate.get();
            double matchScore = matchScoreCalculator.calculateScore(
                    userEntry.getSkillSnapshot(),
                    candidateEntry.getSkillSnapshot()
            );

            roleplayQueueService.removeMatchedUsers(userId, candidateEntry.getUserId());
            saveMatchHistory(userId, candidateEntry.getUserId(), SessionType.ROLEPLAY, matchScore);
            RoleplaySessionResponse session = roleplaySessionService.createSession(CreateRoleplaySessionRequest.builder()
                    .participantAId(userId)
                    .participantBId(candidateEntry.getUserId())
                    .build());
            eventPublisherService.publishMatchFound(Map.of(
                    "sessionType", SessionType.ROLEPLAY,
                    "sessionId", session.getSessionId(),
                    "userAId", userId,
                    "userBId", candidateEntry.getUserId(),
                    "matchScore", matchScore
            ));

            return candidate;
        } finally {
            matchmakingLock.unlock();
        }
    }

    private void saveMatchHistory(
            Long userAId,
            Long userBId,
            SessionType sessionType,
            double matchScore
    ) {
        User userA = userRepository.findById(userAId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User userB = userRepository.findById(userBId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MatchHistory matchHistory = MatchHistory.builder()
                .userA(userA)
                .userB(userB)
                .sessionType(sessionType)
                .matchScore(matchScore)
                .build();

        matchHistoryRepository.save(matchHistory);
    }

    private DebateQueueEntry findDebateQueueEntry(Long userId) {
        return debateQueueService.getAllEntries()
                .stream()
                .filter(entry -> entry.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Debate queue entry not found"));
    }

    private RoleplayQueueEntry findRoleplayQueueEntry(Long userId) {
        return roleplayQueueService.getAllEntries()
                .stream()
                .filter(entry -> entry.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Roleplay queue entry not found"));
    }

    private boolean isOverallCompatible(
            DebateQueueEntry first,
            DebateQueueEntry second
    ) {
        return Math.abs(normalizeOverallScore(first.getSkillSnapshot().getOverallScore())
                - normalizeOverallScore(second.getSkillSnapshot().getOverallScore())) <= 15;
    }

    private boolean isOverallCompatible(
            RoleplayQueueEntry first,
            RoleplayQueueEntry second
    ) {
        return Math.abs(normalizeOverallScore(first.getSkillSnapshot().getOverallScore())
                - normalizeOverallScore(second.getSkillSnapshot().getOverallScore())) <= 15;
    }

    private double normalizeOverallScore(Double overallScore) {
        if (overallScore == null) {
            return AppConstants.INITIAL_OVERALL_SCORE;
        }

        return Math.max(
                AppConstants.MIN_SKILL_SCORE,
                Math.min(AppConstants.MAX_SKILL_SCORE, overallScore)
        );
    }
}
