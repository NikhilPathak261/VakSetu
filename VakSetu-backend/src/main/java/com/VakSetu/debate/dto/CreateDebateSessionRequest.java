package com.vaksetu.debate.dto;

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
public class CreateDebateSessionRequest {

    @NotNull
    private Long topicId;

    @NotNull
    private Long participantAId;

    @NotNull
    private Long participantBId;
}
