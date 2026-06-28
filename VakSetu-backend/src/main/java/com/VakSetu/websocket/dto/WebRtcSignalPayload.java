package com.vaksetu.websocket.dto;

import com.vaksetu.common.enums.SessionType;
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
public class WebRtcSignalPayload {

    private Long senderUserId;
    private Long receiverUserId;
    private Long sessionId;
    private SessionType sessionType;
    private String signalType;
    private Object signalData;
}
