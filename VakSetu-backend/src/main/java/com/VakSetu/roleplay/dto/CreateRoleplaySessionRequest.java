package com.vaksetu.roleplay.dto;

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
public class CreateRoleplaySessionRequest {

    @NotNull
    private Long participantAId;

    @NotNull
    private Long participantBId;
}
