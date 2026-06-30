package com.vaksetu.roleplay.dto;

import com.vaksetu.common.enums.DifficultyLevel;
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
public class RoleplayScenarioResponse {

    private Long id;
    private String title;
    private String description;
    private String roleA;
    private String roleB;
    private DifficultyLevel difficulty;
    private Boolean active;
}
