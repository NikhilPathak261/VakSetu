package com.vaksetu.gd.service;

import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.exception.BadRequestException;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.gd.dto.GiveStarRequest;
import com.vaksetu.gd.dto.StarResponse;
import com.vaksetu.gd.entity.GDParticipant;
import com.vaksetu.gd.entity.GroupDiscussionSession;
import com.vaksetu.gd.entity.SessionStar;
import com.vaksetu.gd.repository.GDParticipantRepository;
import com.vaksetu.gd.repository.GDSessionRepository;
import com.vaksetu.gd.repository.SessionStarRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GDStarService {

    private final GDSessionRepository gdSessionRepository;
    private final GDParticipantRepository gdParticipantRepository;
    private final SessionStarRepository sessionStarRepository;
    private final UserRepository userRepository;

    @Transactional
    public StarResponse giveStar(
            Long giverId,
            GiveStarRequest request
    ) {
        GroupDiscussionSession session = gdSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Group discussion session not found"));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Room is not active");
        }

        GDParticipant giverParticipant = gdParticipantRepository
                .findBySessionIdAndUserIdAndLeftAtIsNull(request.getSessionId(), giverId)
                .orElseThrow(() -> new ResourceNotFoundException("Giver is not an active participant"));

        GDParticipant receiverParticipant = gdParticipantRepository
                .findBySessionIdAndUserIdAndLeftAtIsNull(request.getSessionId(), request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver is not an active participant"));

        if (giverParticipant.getUser().getId().equals(receiverParticipant.getUser().getId())) {
            throw new BadRequestException("Cannot give star to yourself");
        }

        if (!Boolean.TRUE.equals(receiverParticipant.getHasSpoken())) {
            throw new BadRequestException("Receiver has not spoken yet");
        }

        sessionStarRepository.findBySessionIdAndGiverIdAndReceiverId(
                        request.getSessionId(),
                        giverId,
                        request.getReceiverId()
                )
                .ifPresent(star -> {
                    throw new BadRequestException("Star already given");
                });

        User giver = giverParticipant.getUser();
        User receiver = receiverParticipant.getUser();

        SessionStar sessionStar = SessionStar.builder()
                .session(session)
                .giver(giver)
                .receiver(receiver)
                .build();

        sessionStarRepository.save(sessionStar);

        int totalStars = receiver.getTotalStars() == null ? 0 : receiver.getTotalStars();
        receiver.setTotalStars(totalStars + 1);
        userRepository.save(receiver);

        return StarResponse.builder()
                .sessionId(session.getId())
                .giverId(giver.getId())
                .receiverId(receiver.getId())
                .receiverName(receiver.getName())
                .message("Star given successfully")
                .build();
    }
}
