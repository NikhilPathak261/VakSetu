package com.vaksetu.common.mapper;

import com.vaksetu.debate.dto.DebateRuntimeResponse;
import com.vaksetu.debate.dto.DebateSessionResponse;
import com.vaksetu.debate.entity.DebateSession;

public final class DebateMapper {

    private DebateMapper() {
    }

    public static DebateSessionResponse toSessionResponse(DebateSession session) {
        return DebateSessionResponse.builder()
                .id(session.getId())
                .topicId(session.getTopic().getId())
                .topicTitle(session.getTopic().getTitle())
                .participantAId(session.getParticipantA().getId())
                .participantAName(session.getParticipantA().getName())
                .participantBId(session.getParticipantB().getId())
                .participantBName(session.getParticipantB().getName())
                .sideA(session.getSideA())
                .sideB(session.getSideB())
                .status(session.getStatus())
                .currentRound(session.getCurrentRound())
                .totalRounds(session.getTotalRounds())
                .roundStartTime(session.getRoundStartTime())
                .roundEndTime(session.getRoundEndTime())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .build();
    }

    public static DebateRuntimeResponse toRuntimeResponse(DebateSession session) {
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
