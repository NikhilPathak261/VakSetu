package com.vaksetu.gd.service;

import com.vaksetu.common.constants.AppConstants;
import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.exception.BadRequestException;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.gd.dto.CreateGDSessionRequest;
import com.vaksetu.gd.dto.GDSessionResponse;
import com.vaksetu.gd.dto.JoinLeaveRoomResponse;
import com.vaksetu.gd.dto.MarkSpokenResponse;
import com.vaksetu.gd.entity.GDParticipant;
import com.vaksetu.gd.entity.GroupDiscussionSession;
import com.vaksetu.gd.repository.GDParticipantRepository;
import com.vaksetu.gd.repository.GDSessionRepository;
import com.vaksetu.gd.repository.SessionStarRepository;
import com.vaksetu.statistics.service.StatisticsService;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.websocket.service.EventPublisherService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GDSessionService {

    private final GDSessionRepository gdSessionRepository;
    private final GDParticipantRepository gdParticipantRepository;
    private final SessionStarRepository sessionStarRepository;
    private final UserRepository userRepository;
    private final ContributorBadgeService contributorBadgeService;
    private final StatisticsService statisticsService;
    private final EventPublisherService eventPublisherService;

    @Transactional
    public GDSessionResponse createRoom(
            Long creatorId,
            CreateGDSessionRequest request
    ) {
        validateMaxParticipants(request.getMaxParticipants());

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found"));

        GroupDiscussionSession session = GroupDiscussionSession.builder()
                .creator(creator)
                .topic(request.getTopic())
                .maxParticipants(request.getMaxParticipants())
                .currentParticipants(1)
                .status(SessionStatus.ACTIVE)
                .build();

        GroupDiscussionSession savedSession = gdSessionRepository.save(session);

        GDParticipant creatorParticipant = GDParticipant.builder()
                .session(savedSession)
                .user(creator)
                .hasSpoken(false)
                .joinedAt(LocalDateTime.now())
                .leftAt(null)
                .build();

        gdParticipantRepository.save(creatorParticipant);

        statisticsService.incrementGdSessionsJoined(creator);
        GDSessionResponse response = mapToResponse(savedSession);
        eventPublisherService.publishGdRoomCreated(savedSession.getId(), creator.getId(), response);

        return response;
    }

    @Transactional(readOnly = true)
    public GDSessionResponse getRoom(Long sessionId) {
        return mapToResponse(loadRoom(sessionId));
    }

    @Transactional(readOnly = true)
    public List<GDSessionResponse> getActiveRooms() {
        return gdSessionRepository.findByStatus(SessionStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public JoinLeaveRoomResponse joinRoom(
            Long userId,
            Long sessionId
    ) {
        GroupDiscussionSession session = loadRoom(sessionId);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Room is not active");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (gdParticipantRepository.existsBySessionIdAndUserIdAndLeftAtIsNull(sessionId, userId)) {
            throw new BadRequestException("Already joined this room");
        }

        if (session.getCurrentParticipants() >= session.getMaxParticipants()) {
            throw new BadRequestException("Room is full");
        }

        GDParticipant participant = GDParticipant.builder()
                .session(session)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .leftAt(null)
                .hasSpoken(false)
                .build();

        gdParticipantRepository.save(participant);

        session.setCurrentParticipants(session.getCurrentParticipants() + 1);
        GroupDiscussionSession savedSession = gdSessionRepository.save(session);

        statisticsService.incrementGdSessionsJoined(user);
        JoinLeaveRoomResponse response = mapToJoinLeaveRoomResponse(savedSession, user, "Joined successfully");
        eventPublisherService.publishUserJoined(savedSession.getId(), user.getId(), response);

        return response;
    }

    @Transactional
    public JoinLeaveRoomResponse leaveRoom(
            Long userId,
            Long sessionId
    ) {
        GroupDiscussionSession session = loadRoom(sessionId);

        GDParticipant participant = gdParticipantRepository.findBySessionIdAndUserIdAndLeftAtIsNull(
                        sessionId,
                        userId
                )
                .orElseThrow(() -> new ResourceNotFoundException("Active participant not found"));

        participant.setLeftAt(LocalDateTime.now());
        gdParticipantRepository.save(participant);

        int currentParticipants = Math.max(0, session.getCurrentParticipants() - 1);
        session.setCurrentParticipants(currentParticipants);

        if (currentParticipants == 0) {
            session.setStatus(SessionStatus.COMPLETED);
        } else if (session.getCreator().getId().equals(userId)) {
            GDParticipant oldestActiveParticipant = gdParticipantRepository
                    .findFirstBySessionIdAndLeftAtIsNullOrderByJoinedAtAsc(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Active participant not found"));

            session.setCreator(oldestActiveParticipant.getUser());
        }

        GroupDiscussionSession savedSession = gdSessionRepository.save(session);
        JoinLeaveRoomResponse response = mapToJoinLeaveRoomResponse(savedSession, participant.getUser(), "Left successfully");
        eventPublisherService.publishUserLeft(savedSession.getId(), participant.getUser().getId(), response);

        return response;
    }

    @Transactional
    public MarkSpokenResponse markSpoken(
            Long userId,
            Long sessionId
    ) {
        GroupDiscussionSession session = loadRoom(sessionId);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Room is not active");
        }

        GDParticipant participant = gdParticipantRepository.findBySessionIdAndUserIdAndLeftAtIsNull(
                        sessionId,
                        userId
                )
                .orElseThrow(() -> new ResourceNotFoundException("Active participant not found"));

        participant.setHasSpoken(true);
        GDParticipant savedParticipant = gdParticipantRepository.save(participant);

        return MarkSpokenResponse.builder()
                .sessionId(session.getId())
                .userId(savedParticipant.getUser().getId())
                .userName(savedParticipant.getUser().getName())
                .hasSpoken(savedParticipant.getHasSpoken())
                .message("Participant marked as spoken")
                .build();
    }

    @Transactional
    public GDSessionResponse closeRoom(
            Long creatorId,
            Long sessionId
    ) {
        GroupDiscussionSession session = loadRoom(sessionId);

        if (!session.getCreator().getId().equals(creatorId)) {
            throw new BadRequestException("Only room creator can close room");
        }

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Room is not active");
        }

        List<GDParticipant> participants = gdParticipantRepository.findBySessionId(sessionId);
        Map<Long, Long> starsByUserId = participants.stream()
                .collect(Collectors.toMap(
                        participant -> participant.getUser().getId(),
                        participant -> sessionStarRepository.countBySessionIdAndReceiverId(
                                sessionId,
                                participant.getUser().getId()
                        ),
                        Long::sum
                ));

        List<GDParticipant> topParticipants = participants.stream()
                .sorted(Comparator.comparing(
                        participant -> starsByUserId.getOrDefault(participant.getUser().getId(), 0L),
                        Comparator.reverseOrder()
                ))
                .limit(3)
                .toList();

        if (!topParticipants.isEmpty()) {
            User winner = topParticipants.get(0).getUser();
            statisticsService.incrementTopContributorFinishes(winner);
            contributorBadgeService.updateBadge(winner);
        }

        participants.forEach(participant -> statisticsService.updateHighestSessionStars(
                participant.getUser(),
                starsByUserId.getOrDefault(participant.getUser().getId(), 0L).intValue()
        ));

        session.setStatus(SessionStatus.COMPLETED);
        GDSessionResponse response = mapToResponse(gdSessionRepository.save(session));
        eventPublisherService.publishLeaderboardUpdated(session.getId(), starsByUserId);
        eventPublisherService.publishGdRoomClosed(session.getId(), creatorId, response);

        return response;
    }

    @Transactional(readOnly = true)
    public GroupDiscussionSession loadRoom(Long sessionId) {
        return gdSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Group discussion session not found"));
    }

    public GDSessionResponse mapToResponse(GroupDiscussionSession session) {
        return GDSessionResponse.builder()
                .sessionId(session.getId())
                .topic(session.getTopic())
                .creatorId(session.getCreator().getId())
                .creatorName(session.getCreator().getName())
                .status(session.getStatus())
                .currentParticipants(session.getCurrentParticipants())
                .maxParticipants(session.getMaxParticipants())
                .createdAt(session.getCreatedAt())
                .build();
    }

    private JoinLeaveRoomResponse mapToJoinLeaveRoomResponse(
            GroupDiscussionSession session,
            User user,
            String message
    ) {
        return JoinLeaveRoomResponse.builder()
                .sessionId(session.getId())
                .userId(user.getId())
                .userName(user.getName())
                .message(message)
                .currentParticipants(session.getCurrentParticipants())
                .maxParticipants(session.getMaxParticipants())
                .build();
    }

    private void validateMaxParticipants(Integer maxParticipants) {
        if (maxParticipants == null
                || maxParticipants < 1
                || maxParticipants > AppConstants.MAX_GD_PARTICIPANTS) {
            throw new BadRequestException("Invalid maximum participant count");
        }
    }
}
