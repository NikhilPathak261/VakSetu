package com.vaksetu.feedback.controller;

import com.vaksetu.feedback.dto.FeedbackResponse;
import com.vaksetu.feedback.dto.SubmitFeedbackRequest;
import com.vaksetu.feedback.service.FeedbackService;
import com.vaksetu.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public FeedbackResponse submitFeedback(
            @Valid @RequestBody SubmitFeedbackRequest request
    ) {
        return feedbackService.submitFeedback(getAuthenticatedUserId(), request);
    }

    private Long getAuthenticatedUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return userDetails.getId();
    }
}
