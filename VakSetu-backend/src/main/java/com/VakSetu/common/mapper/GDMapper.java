package com.vaksetu.common.mapper;

import com.vaksetu.gd.dto.GDSessionResponse;
import com.vaksetu.gd.dto.JoinLeaveRoomResponse;
import com.vaksetu.gd.dto.LeaderboardEntryResponse;
import com.vaksetu.gd.entity.GDParticipant;
import com.vaksetu.gd.entity.GroupDiscussionSession;
import com.vaksetu.user.entity.User;

public final class GDMapper {

    private GDMapper() {
    }

    public static GDSessionResponse toSessionResponse(GroupDiscussionSession session) {
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

    public static JoinLeaveRoomResponse toJoinLeaveRoomResponse(
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

    public static LeaderboardEntryResponse toLeaderboardEntry(
            GDParticipant participant,
            long stars
    ) {
        return LeaderboardEntryResponse.builder()
                .userId(participant.getUser().getId())
                .userName(participant.getUser().getName())
                .stars(stars)
                .build();
    }
}
