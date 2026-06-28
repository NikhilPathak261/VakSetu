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
public class StarResponse {

    private Long sessionId;
    private Long giverId;
    private Long receiverId;
    private String receiverName;
    private String message;
}
