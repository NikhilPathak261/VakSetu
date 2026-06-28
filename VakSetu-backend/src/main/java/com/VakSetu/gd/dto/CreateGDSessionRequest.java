package com.vaksetu.gd.dto;

import com.vaksetu.common.constants.AppConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class CreateGDSessionRequest {

    @NotBlank
    private String topic;

    @NotNull
    @Min(1)
    @Max(AppConstants.MAX_GD_PARTICIPANTS)
    private Integer maxParticipants;
}
