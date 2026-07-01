package com.vaksetu.matchmaking.service;

import com.vaksetu.exception.ConflictException;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.matchmaking.dto.UserSkillSnapshot;
import com.vaksetu.matchmaking.queue.DebateQueueEntry;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.user.repository.UserSkillRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DebateQueueService {

    private final Map<Long, DebateQueueEntry> queue = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;

    public void addUser(Long userId, Long topicId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserSkill userSkill = userSkillRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));

        DebateQueueEntry queueEntry = DebateQueueEntry.builder()
                .userId(userId)
                .topicId(topicId)
                .skillSnapshot(buildUserSkillSnapshot(user, userSkill))
                .joinedAt(LocalDateTime.now())
                .build();

        if (queue.putIfAbsent(userId, queueEntry) != null) {
            throw new ConflictException("User already in debate queue");
        }
    }

    public void removeUser(Long userId) {
        queue.remove(userId);
    }

    public void removeMatchedUsers(
            Long userAId,
            Long userBId
    ) {
        queue.remove(userAId);
        queue.remove(userBId);
    }

    public Collection<DebateQueueEntry> getAllEntries() {
        return queue.values();
    }

    public boolean containsUser(Long userId) {
        return queue.containsKey(userId);
    }

    public int size() {
        return queue.size();
    }

    public int removeEntriesJoinedBefore(LocalDateTime cutoffTime) {
        List<Long> expiredUserIds = queue.values()
                .stream()
                .filter(entry -> entry.getJoinedAt().isBefore(cutoffTime))
                .map(DebateQueueEntry::getUserId)
                .toList();

        expiredUserIds.forEach(queue::remove);

        return expiredUserIds.size();
    }

    private UserSkillSnapshot buildUserSkillSnapshot(User user, UserSkill userSkill) {
        return UserSkillSnapshot.builder()
                .overallScore(user.getOverallScore())
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
