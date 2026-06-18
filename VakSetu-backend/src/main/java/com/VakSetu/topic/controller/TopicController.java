package com.vaksetu.topic.controller;

import com.vaksetu.topic.dto.CreateTopicRequest;
import com.vaksetu.topic.dto.TopicResponse;
import com.vaksetu.topic.service.TopicService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public List<TopicResponse> getAllActiveTopics() {
        return topicService.getAllActiveTopics();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TopicResponse createTopic(
            @Valid @RequestBody CreateTopicRequest request
    ) {
        return topicService.createTopic(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateTopic(
            @PathVariable Long id
    ) {
        topicService.deactivateTopic(id);
        return ResponseEntity.noContent().build();
    }
}
