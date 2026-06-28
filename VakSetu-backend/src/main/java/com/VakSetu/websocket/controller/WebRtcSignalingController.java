package com.vaksetu.websocket.controller;

import com.vaksetu.websocket.dto.WebRtcSignalRequest;
import com.vaksetu.websocket.service.WebRtcSignalingService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebRtcSignalingController {

    private final WebRtcSignalingService webRtcSignalingService;

    @MessageMapping("/webrtc/signal")
    public void signal(
            @Valid WebRtcSignalRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        webRtcSignalingService.forwardSignal(getAuthenticatedUserId(headerAccessor), request);
    }

    private Long getAuthenticatedUserId(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes == null || sessionAttributes.get("userId") == null) {
            throw new IllegalStateException("WebSocket user is not authenticated");
        }

        return (Long) sessionAttributes.get("userId");
    }
}
