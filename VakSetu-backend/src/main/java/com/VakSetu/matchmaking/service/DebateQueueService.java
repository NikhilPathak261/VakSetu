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
import java.util.Map;
import java.util.Optional;
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
        if (queue.containsKey(userId)) {
            throw new ConflictException("User already in debate queue");
        }

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

        queue.put(userId, queueEntry);
    }

    public void removeUser(Long userId) {
        queue.remove(userId);
    }

    public Optional<DebateQueueEntry> findCandidate(
            Long userId,
            Long topicId
    ) {
        return queue.values()
                .stream()
                .filter(candidate -> !candidate.getUserId().equals(userId))
                .filter(candidate -> candidate.getTopicId().equals(topicId))
                .findFirst();
    }

    public Collection<DebateQueueEntry> getAllEntries() {
        return queue.values();
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
