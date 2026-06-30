package com.vaksetu.topic.service;

import com.vaksetu.common.mapper.TopicMapper;
import com.vaksetu.exception.ConflictException;
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
                .map(TopicMapper::toResponse)
                .toList();
    }

    @Transactional
    public TopicResponse createTopic(CreateTopicRequest request) {
        if (topicRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new ConflictException("Topic already exists");
        }

        Topic topic = Topic.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .active(true)
                .build();

        Topic savedTopic = topicRepository.save(topic);

        return TopicMapper.toResponse(savedTopic);
    }

    @Transactional
    public void deactivateTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        topic.setActive(false);
    }
}
