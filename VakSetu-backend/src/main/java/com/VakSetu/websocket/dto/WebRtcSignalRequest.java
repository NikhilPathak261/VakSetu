package com.vaksetu.websocket.dto;

import com.vaksetu.common.enums.SessionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class WebRtcSignalRequest {

    @NotNull
    private Long sessionId;

    @NotNull
    private SessionType sessionType;

    private Long receiverUserId;

    @NotBlank
    private String signalType;

    @NotNull
    private Object signalData;
}
