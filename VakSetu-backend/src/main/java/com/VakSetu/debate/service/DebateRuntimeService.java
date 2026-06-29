package com.vaksetu.debate.service;

import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.debate.dto.DebateRuntimeResponse;
import com.vaksetu.debate.entity.DebateSession;
import com.vaksetu.debate.repository.DebateSessionRepository;
import com.vaksetu.exception.BadRequestException;
import com.vaksetu.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DebateRuntimeService {

    private final DebateSessionRepository debateSessionRepository;

    @Transactional
    public DebateRuntimeResponse moveToPreparation(Long sessionId) {
        DebateSession session = loadSession(sessionId);
        validateStatus(session, SessionStatus.MATCHED, "Cannot move to PREPARATION unless status is MATCHED");
        LocalDateTime now = LocalDateTime.now();

        session.setStatus(SessionStatus.PREPARATION);
        session.setCurrentRound(0);
        session.setCurrentSpeaker(null);
        session.setRoundStartTime(now);
        session.setRoundEndTime(now.plusSeconds(session.getPreparationSeconds()));

        return mapToRuntimeResponse(debateSessionRepository.save(session));
    }

    @Transactional
    public DebateRuntimeResponse startRoundOne(Long sessionId) {
        DebateSession session = loadSession(sessionId);
        validateStatus(session, SessionStatus.PREPARATION, "Cannot start ROUND_1 unless status is PREPARATION");
        LocalDateTime now = LocalDateTime.now();

        session.setStatus(SessionStatus.ROUND_1);
        session.setCurrentRound(1);
        session.setCurrentSpeaker(session.getParticipantA());
        session.setStartTime(now);
        session.setRoundStartTime(now);
        session.setRoundEndTime(now.plusSeconds(session.getRoundDurationSeconds()));

        return mapToRuntimeResponse(debateSessionRepository.save(session));
    }

    @Transactional
    public DebateRuntimeResponse startRoundTwo(Long sessionId) {
        DebateSession session = loadSession(sessionId);
        validateStatus(session, SessionStatus.ROUND_1, "Cannot start ROUND_2 unless status is ROUND_1");
        LocalDateTime now = LocalDateTime.now();

        session.setStatus(SessionStatus.ROUND_2);
        session.setCurrentRound(2);
        session.setCurrentSpeaker(session.getParticipantB());
        session.setRoundStartTime(now);
        session.setRoundEndTime(now.plusSeconds(session.getRoundDurationSeconds()));

        return mapToRuntimeResponse(debateSessionRepository.save(session));
    }

    @Transactional
    public DebateRuntimeResponse startRoundThree(Long sessionId) {
        DebateSession session = loadSession(sessionId);
        validateStatus(session, SessionStatus.ROUND_2, "Cannot start ROUND_3 unless status is ROUND_2");
        LocalDateTime now = LocalDateTime.now();

        session.setStatus(SessionStatus.ROUND_3);
        session.setCurrentRound(3);
        session.setCurrentSpeaker(session.getParticipantA());
        session.setRoundStartTime(now);
        session.setRoundEndTime(now.plusSeconds(session.getRoundDurationSeconds()));

        return mapToRuntimeResponse(debateSessionRepository.save(session));
    }

    @Transactional
    public DebateRuntimeResponse completeDebate(Long sessionId) {
        throw new BadRequestException("Submit required feedback to complete debate session");
    }

    @Transactional(readOnly = true)
    public DebateRuntimeResponse getRuntime(Long sessionId) {
        return mapToRuntimeResponse(loadSession(sessionId));
    }

    private DebateSession loadSession(Long sessionId) {
        return debateSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Debate session not found"));
    }

    private void validateStatus(
            DebateSession session,
            SessionStatus expectedStatus,
            String message
    ) {
        if (session.getStatus() != expectedStatus) {
            throw new BadRequestException(message);
        }
    }

    private DebateRuntimeResponse mapToRuntimeResponse(DebateSession session) {
        Long currentSpeakerId = session.getCurrentSpeaker() == null
                ? null
                : session.getCurrentSpeaker().getId();
        String currentSpeakerName = session.getCurrentSpeaker() == null
                ? null
                : session.getCurrentSpeaker().getName();

        return DebateRuntimeResponse.builder()
                .sessionId(session.getId())
                .status(session.getStatus())
                .currentRound(session.getCurrentRound())
                .totalRounds(session.getTotalRounds())
                .currentSpeakerId(currentSpeakerId)
                .currentSpeakerName(currentSpeakerName)
                .roundStartTime(session.getRoundStartTime())
                .roundEndTime(session.getRoundEndTime())
                .preparationSeconds(session.getPreparationSeconds())
                .roundDurationSeconds(session.getRoundDurationSeconds())
                .build();
    }
}
