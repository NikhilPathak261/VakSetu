package com.vaksetu.gd.dto;

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
public class GiveStarRequest {

    @NotNull
    private Long sessionId;

    @NotNull
    private Long receiverId;
}
