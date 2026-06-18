package com.vaksetu.topic.service;

import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.topic.dto.CreateTopicRequest;
import com.vaksetu.topic.dto.TopicResponse;
import com.vaksetu.topic.entity.Topic;
import com.vaksetu.topic.repository.TopicRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;

    public List<TopicResponse> getAllActiveTopics() {
        return topicRepository.findByActiveTrue()
                .stream()
                .map(this::toTopicResponse)
                .toList();
    }

    @Transactional
    public TopicResponse createTopic(CreateTopicRequest request) {
        Topic topic = Topic.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .active(true)
                .build();

        Topic savedTopic = topicRepository.save(topic);

        return toTopicResponse(savedTopic);
    }

    @Transactional
    public void deactivateTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        topic.setActive(false);
    }

    private TopicResponse toTopicResponse(Topic topic) {
        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .category(topic.getCategory())
                .active(topic.getActive())
                .build();
    }
}
