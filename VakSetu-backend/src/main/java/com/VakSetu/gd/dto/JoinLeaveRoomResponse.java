package com.vaksetu.gd.dto;

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
public class JoinLeaveRoomResponse {

    private Long sessionId;
    private Long userId;
    private String userName;
    private String message;
    private Integer currentParticipants;
    private Integer maxParticipants;
}
