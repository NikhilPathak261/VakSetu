package com.vaksetu.user.controller;

import com.vaksetu.security.CustomUserDetails;
import com.vaksetu.user.dto.UpdateProfileRequest;
import com.vaksetu.user.dto.UserProfileResponse;
import com.vaksetu.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponse getProfile() {
        return userService.getProfile(getAuthenticatedUserId());
    }

    @PutMapping("/me")
    public UserProfileResponse updateProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateProfile(getAuthenticatedUserId(), request);
    }

    private Long getAuthenticatedUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return userDetails.getId();
    }
}
