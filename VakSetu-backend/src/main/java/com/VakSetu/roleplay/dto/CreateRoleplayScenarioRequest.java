package com.vaksetu.roleplay.dto;

import com.vaksetu.common.enums.DifficultyLevel;
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
public class CreateRoleplayScenarioRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String roleA;

    @NotBlank
    private String roleB;

    @NotNull
    private DifficultyLevel difficulty;
}
