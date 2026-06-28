package com.vaksetu.websocket.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketEvent {

    private String eventType;
    private Long sessionId;
    private Long userId;
    private String message;
    private Object payload;
    private LocalDateTime timestamp;
}
