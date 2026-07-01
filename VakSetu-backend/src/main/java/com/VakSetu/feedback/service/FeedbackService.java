package com.vaksetu.feedback.service;

import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.common.enums.SessionType;
import com.vaksetu.debate.entity.DebateSession;
import com.vaksetu.debate.repository.DebateSessionRepository;
import com.vaksetu.exception.BadRequestException;
import com.vaksetu.exception.ConflictException;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.feedback.dto.FeedbackResponse;
import com.vaksetu.feedback.dto.SkillRatingScores;
import com.vaksetu.feedback.dto.SubmitFeedbackRequest;
import com.vaksetu.feedback.entity.Feedback;
import com.vaksetu.feedback.repository.FeedbackRepository;
import com.vaksetu.reputation.service.ReputationService;
import com.vaksetu.roleplay.entity.RoleplaySession;
import com.vaksetu.roleplay.repository.RoleplaySessionRepository;
import com.vaksetu.skill.service.SkillService;
import com.vaksetu.statistics.service.StatisticsService;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final DebateSessionRepository debateSessionRepository;
    private final RoleplaySessionRepository roleplaySessionRepository;
    private final RatingCalculationService ratingCalculationService;
    private final SkillService skillService;
    private final ReputationService reputationService;
    private final StatisticsService statisticsService;

    @Transactional
    public FeedbackResponse submitFeedback(
            Long evaluatorId,
            SubmitFeedbackRequest request
    ) {
        validateSupportedSessionType(request.getSessionType());

        User evaluator = userRepository.findById(evaluatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator not found"));
        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        SessionParticipants participants = loadAndValidateParticipants(request, evaluator, targetUser);
        validateDuplicateFeedback(evaluatorId, request);

        Feedback feedback = feedbackRepository.save(Feedback.builder()
                .sessionId(request.getSessionId())
                .sessionType(request.getSessionType())
                .evaluator(evaluator)
                .targetUser(targetUser)
                .fluencyRating(request.getFluencyRating())
                .pronunciationRating(request.getPronunciationRating())
                .grammarRating(request.getGrammarRating())
                .confidenceRating(request.getConfidenceRating())
                .empathyRating(request.getEmpathyRating())
                .listeningRating(request.getListeningRating())
                .engagementRating(request.getEngagementRating())
                .build());

        boolean sessionCompleted = completeSessionIfReady(request, participants);

        return FeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .sessionId(feedback.getSessionId())
                .sessionType(feedback.getSessionType())
                .evaluatorId(evaluator.getId())
                .targetUserId(targetUser.getId())
                .sessionCompleted(sessionCompleted)
                .message(sessionCompleted
                        ? "Feedback submitted and session completed"
                        : "Feedback submitted")
                .build();
    }

    private SessionParticipants loadAndValidateParticipants(
            SubmitFeedbackRequest request,
            User evaluator,
            User targetUser
    ) {
        if (request.getSessionType() == SessionType.DEBATE) {
            DebateSession session = debateSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Debate session not found"));
            validateDebateFeedbackState(session);

            return validateParticipants(
                    evaluator,
                    targetUser,
                    session.getParticipantA(),
                    session.getParticipantB()
            );
        }

        RoleplaySession session = roleplaySessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Roleplay session not found"));
        validateRoleplayFeedbackState(session);

        return validateParticipants(
                evaluator,
                targetUser,
                session.getParticipantA(),
                session.getParticipantB()
        );
    }

    private SessionParticipants validateParticipants(
            User evaluator,
            User targetUser,
            User participantA,
            User participantB
    ) {
        if (evaluator.getId().equals(targetUser.getId())) {
            throw new BadRequestException("Evaluator and target user must be different");
        }

        boolean evaluatorIsParticipant = evaluator.getId().equals(participantA.getId())
                || evaluator.getId().equals(participantB.getId());
        boolean targetIsParticipant = targetUser.getId().equals(participantA.getId())
                || targetUser.getId().equals(participantB.getId());

        if (!evaluatorIsParticipant || !targetIsParticipant) {
            throw new BadRequestException("Feedback users must be session participants");
        }

        return new SessionParticipants(participantA, participantB);
    }

    private void validateDuplicateFeedback(
            Long evaluatorId,
            SubmitFeedbackRequest request
    ) {
        boolean feedbackExists = feedbackRepository.existsBySessionIdAndSessionTypeAndEvaluatorIdAndTargetUserId(
                request.getSessionId(),
                request.getSessionType(),
                evaluatorId,
                request.getTargetUserId()
        );

        if (feedbackExists) {
            throw new ConflictException("Feedback already submitted");
        }
    }

    private boolean completeSessionIfReady(
            SubmitFeedbackRequest request,
            SessionParticipants participants
    ) {
        List<Feedback> feedbackList = feedbackRepository.findBySessionIdAndSessionType(
                request.getSessionId(),
                request.getSessionType()
        );

        if (!hasRequiredFeedback(feedbackList, participants)) {
            return false;
        }

        updateParticipantAfterFeedback(participants.participantA(), request, feedbackList);
        updateParticipantAfterFeedback(participants.participantB(), request, feedbackList);
        markSessionCompleted(request);

        return true;
    }

    private boolean hasRequiredFeedback(
            List<Feedback> feedbackList,
            SessionParticipants participants
    ) {
        return feedbackList.stream().anyMatch(feedback -> isFeedbackPair(
                feedback,
                participants.participantA(),
                participants.participantB()
        )) && feedbackList.stream().anyMatch(feedback -> isFeedbackPair(
                feedback,
                participants.participantB(),
                participants.participantA()
        ));
    }

    private boolean isFeedbackPair(
            Feedback feedback,
            User evaluator,
            User target
    ) {
        return feedback.getEvaluator().getId().equals(evaluator.getId())
                && feedback.getTargetUser().getId().equals(target.getId());
    }

    private void updateParticipantAfterFeedback(
            User participant,
            SubmitFeedbackRequest request,
            List<Feedback> feedbackList
    ) {
        List<Feedback> receivedFeedback = feedbackList.stream()
                .filter(feedback -> feedback.getTargetUser().getId().equals(participant.getId()))
                .toList();
        SkillRatingScores sessionRatings = ratingCalculationService.calculateSessionRatings(receivedFeedback);

        skillService.updateSkills(participant, request.getSessionId(), request.getSessionType(), sessionRatings);
        reputationService.rewardSessionCompletion(participant);

        if (request.getSessionType() == SessionType.DEBATE) {
            statisticsService.incrementDebateCompleted(participant);
        } else {
            statisticsService.incrementRoleplayCompleted(participant);
        }
    }

    private void markSessionCompleted(SubmitFeedbackRequest request) {
        LocalDateTime now = LocalDateTime.now();

        if (request.getSessionType() == SessionType.DEBATE) {
            DebateSession session = debateSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Debate session not found"));
            session.setStatus(SessionStatus.COMPLETED);
            session.setEndTime(now);
            debateSessionRepository.save(session);
            return;
        }

        RoleplaySession session = roleplaySessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Roleplay session not found"));
        session.setStatus(SessionStatus.COMPLETED);
        session.setCurrentPhase(SessionStatus.COMPLETED);
        session.setEndTime(now);
        roleplaySessionRepository.save(session);
    }

    private void validateSupportedSessionType(SessionType sessionType) {
        if (sessionType == SessionType.GD) {
            throw new BadRequestException("Group Discussion feedback does not update skills");
        }
    }

    private void validateDebateFeedbackState(DebateSession session) {
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new BadRequestException("Cannot submit feedback for cancelled session");
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new BadRequestException("Cannot submit feedback for completed session");
        }

        if (session.getStatus() != SessionStatus.ROUND_3) {
            throw new BadRequestException("Debate feedback can be submitted after the final round");
        }

        if (session.getRoundEndTime() == null || session.getRoundEndTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Debate feedback can be submitted after the final round ends");
        }
    }

    private void validateRoleplayFeedbackState(RoleplaySession session) {
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new BadRequestException("Cannot submit feedback for cancelled session");
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new BadRequestException("Cannot submit feedback for completed session");
        }

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Roleplay feedback can be submitted after roleplay is complete");
        }

        if (session.getEndTime() == null || session.getEndTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Roleplay feedback can be submitted after roleplay ends");
        }
    }

    private record SessionParticipants(
            User participantA,
            User participantB
    ) {
    }
}
