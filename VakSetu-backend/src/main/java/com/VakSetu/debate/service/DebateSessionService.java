package com.vaksetu.debate.service;

import com.vaksetu.common.enums.DebateSide;
import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.common.mapper.DebateMapper;
import com.vaksetu.debate.dto.CreateDebateSessionRequest;
import com.vaksetu.debate.dto.DebateSessionResponse;
import com.vaksetu.debate.entity.DebateSession;
import com.vaksetu.debate.repository.DebateSessionRepository;
import com.vaksetu.exception.BadRequestException;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.topic.entity.Topic;
import com.vaksetu.topic.repository.TopicRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DebateSessionService {

    private final DebateSessionRepository debateSessionRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    @Transactional
    public DebateSessionResponse createSession(CreateDebateSessionRequest request) {
        if (request.getParticipantAId().equals(request.getParticipantBId())) {
            throw new BadRequestException("Debate participants must be different");
        }

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        User participantA = userRepository.findById(request.getParticipantAId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant A not found"));
        User participantB = userRepository.findById(request.getParticipantBId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant B not found"));

        boolean participantAFor = ThreadLocalRandom.current().nextBoolean();
        DebateSide sideA = participantAFor ? DebateSide.FOR : DebateSide.AGAINST;
        DebateSide sideB = participantAFor ? DebateSide.AGAINST : DebateSide.FOR;

        DebateSession debateSession = DebateSession.builder()
                .topic(topic)
                .participantA(participantA)
                .participantB(participantB)
                .sideA(sideA)
                .sideB(sideB)
                .status(SessionStatus.MATCHED)
                .currentRound(0)
                .totalRounds(3)
                .preparationSeconds(120)
                .roundDurationSeconds(180)
                .build();

        DebateSession savedSession = debateSessionRepository.save(debateSession);

        return DebateMapper.toSessionResponse(savedSession);
    }

    @Transactional(readOnly = true)
    public DebateSessionResponse getSession(Long sessionId) {
        DebateSession session = loadSession(sessionId);

        return DebateMapper.toSessionResponse(session);
    }

    @Transactional
    public DebateSessionResponse startSession(Long sessionId) {
        DebateSession session = loadSession(sessionId);
        validateStatus(session, SessionStatus.MATCHED, "Cannot start preparation unless status is MATCHED");
        LocalDateTime now = LocalDateTime.now();
        session.setStatus(SessionStatus.PREPARATION);
        session.setCurrentRound(0);
        session.setCurrentSpeaker(null);
        session.setRoundStartTime(now);
        session.setRoundEndTime(now.plusSeconds(session.getPreparationSeconds()));
        DebateSession savedSession = debateSessionRepository.save(session);

        return DebateMapper.toSessionResponse(savedSession);
    }

    @Transactional
    public DebateSessionResponse completeSession(Long sessionId) {
        throw new BadRequestException("Submit required feedback to complete debate session");
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
}
