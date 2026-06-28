package com.vaksetu.websocket.service;

import com.vaksetu.websocket.dto.WebSocketEvent;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisherService {

    public static final String TOPIC_MATCH = "/topic/match";
    public static final String TOPIC_GD = "/topic/gd";
    public static final String TOPIC_DEBATE = "/topic/debate";
    public static final String TOPIC_ROLEPLAY = "/topic/roleplay";
    public static final String TOPIC_SYSTEM = "/topic/system";
    public static final String TOPIC_WEBRTC = "/topic/webrtc";

    public static final String MATCH_FOUND = "MATCH_FOUND";
    public static final String USER_JOINED = "USER_JOINED";
    public static final String USER_LEFT = "USER_LEFT";
    public static final String STAR_RECEIVED = "STAR_RECEIVED";
    public static final String LEADERBOARD_UPDATED = "LEADERBOARD_UPDATED";
    public static final String GD_ROOM_CREATED = "GD_ROOM_CREATED";
    public static final String GD_ROOM_CLOSED = "GD_ROOM_CLOSED";
    public static final String WEBRTC_OFFER = "WEBRTC_OFFER";
    public static final String WEBRTC_ANSWER = "WEBRTC_ANSWER";
    public static final String ICE_CANDIDATE = "ICE_CANDIDATE";

    private final SimpMessagingTemplate messagingTemplate;

    public void publishMatchFound(Object payload) {
        publish(TOPIC_MATCH, MATCH_FOUND, null, null, "Match found", payload);
    }

    public void publishGdRoomCreated(
            Long sessionId,
            Long userId,
            Object payload
    ) {
        publish(TOPIC_GD, GD_ROOM_CREATED, sessionId, userId, "GD room created", payload);
    }

    public void publishUserJoined(
            Long sessionId,
            Long userId,
            Object payload
    ) {
        publish(TOPIC_GD, USER_JOINED, sessionId, userId, "User joined GD room", payload);
    }

    public void publishUserLeft(
            Long sessionId,
            Long userId,
            Object payload
    ) {
        publish(TOPIC_GD, USER_LEFT, sessionId, userId, "User left GD room", payload);
    }

    public void publishStarReceived(
            Long sessionId,
            Long userId,
            Object payload
    ) {
        publish(TOPIC_GD, STAR_RECEIVED, sessionId, userId, "Star received", payload);
    }

    public void publishLeaderboardUpdated(
            Long sessionId,
            Object payload
    ) {
        publish(TOPIC_GD, LEADERBOARD_UPDATED, sessionId, null, "GD leaderboard updated", payload);
    }

    public void publishGdRoomClosed(
            Long sessionId,
            Long userId,
            Object payload
    ) {
        publish(TOPIC_GD, GD_ROOM_CLOSED, sessionId, userId, "GD room closed", payload);
    }

    public void publishWebRtcSignal(
            String eventType,
            Long sessionId,
            Long userId,
            Object payload
    ) {
        publish(
                TOPIC_WEBRTC + "/" + sessionId,
                eventType,
                sessionId,
                userId,
                "WebRTC signaling message",
                payload
        );
    }

    public void publish(
            String destination,
            String eventType,
            Long sessionId,
            Long userId,
            String message,
            Object payload
    ) {
        messagingTemplate.convertAndSend(
                destination,
                WebSocketEvent.builder()
                        .eventType(eventType)
                        .sessionId(sessionId)
                        .userId(userId)
                        .message(message)
                        .payload(payload)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}
