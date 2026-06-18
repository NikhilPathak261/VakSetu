package com.vaksetu.matchmaking.service;

import com.vaksetu.common.enums.SessionType;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.matchmaking.entity.MatchHistory;
import com.vaksetu.matchmaking.queue.DebateQueueEntry;
import com.vaksetu.matchmaking.queue.RoleplayQueueEntry;
import com.vaksetu.matchmaking.repository.MatchHistoryRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final DebateQueueService debateQueueService;
    private final RoleplayQueueService roleplayQueueService;
    private final MatchHistoryRepository matchHistoryRepository;
    private final UserRepository userRepository;
    private final MatchScoreCalculator matchScoreCalculator;

    public Optional<DebateQueueEntry> findDebateMatch(
            Long userId,
            Long topicId
    ) {
        Optional<DebateQueueEntry> candidate = debateQueueService.findCandidate(userId, topicId);

        if (candidate.isEmpty()) {
            return Optional.empty();
        }

        DebateQueueEntry userEntry = findDebateQueueEntry(userId);
        DebateQueueEntry candidateEntry = candidate.get();
        double matchScore = matchScoreCalculator.calculateScore(
                userEntry.getSkillSnapshot(),
                candidateEntry.getSkillSnapshot()
        );

        debateQueueService.removeUser(userId);
        debateQueueService.removeUser(candidateEntry.getUserId());
        saveMatchHistory(userId, candidateEntry.getUserId(), SessionType.DEBATE, matchScore);

        return candidate;
    }

    public Optional<RoleplayQueueEntry> findRoleplayMatch(Long userId) {
        Optional<RoleplayQueueEntry> candidate = roleplayQueueService.findCandidate(userId);

        if (candidate.isEmpty()) {
            return Optional.empty();
        }

        RoleplayQueueEntry userEntry = findRoleplayQueueEntry(userId);
        RoleplayQueueEntry candidateEntry = candidate.get();
        double matchScore = matchScoreCalculator.calculateScore(
                userEntry.getSkillSnapshot(),
                candidateEntry.getSkillSnapshot()
        );

        roleplayQueueService.removeUser(userId);
        roleplayQueueService.removeUser(candidateEntry.getUserId());
        saveMatchHistory(userId, candidateEntry.getUserId(), SessionType.ROLEPLAY, matchScore);

        return candidate;
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
}
