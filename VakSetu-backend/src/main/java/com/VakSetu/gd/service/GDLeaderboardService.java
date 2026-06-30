package com.vaksetu.gd.service;

import com.vaksetu.common.mapper.GDMapper;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.gd.dto.GDLeaderboardResponse;
import com.vaksetu.gd.dto.LeaderboardEntryResponse;
import com.vaksetu.gd.entity.GDParticipant;
import com.vaksetu.gd.repository.GDParticipantRepository;
import com.vaksetu.gd.repository.GDSessionRepository;
import com.vaksetu.gd.repository.SessionStarRepository;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GDLeaderboardService {

    private final GDSessionRepository gdSessionRepository;
    private final GDParticipantRepository gdParticipantRepository;
    private final SessionStarRepository sessionStarRepository;

    @Transactional(readOnly = true)
    public GDLeaderboardResponse getLeaderboard(Long sessionId) {
        if (!gdSessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Group discussion session not found");
        }

        return GDLeaderboardResponse.builder()
                .leaderboard(gdParticipantRepository.findBySessionIdAndLeftAtIsNull(sessionId)
                        .stream()
                        .map(participant -> mapToEntry(sessionId, participant))
                        .sorted(Comparator.comparing(LeaderboardEntryResponse::getStars).reversed())
                        .limit(3)
                        .toList())
                .build();
    }

    private LeaderboardEntryResponse mapToEntry(
            Long sessionId,
            GDParticipant participant
    ) {
        long stars = sessionStarRepository.countBySessionIdAndReceiverId(
                sessionId,
                participant.getUser().getId()
        );

        return GDMapper.toLeaderboardEntry(participant, stars);
    }
}
