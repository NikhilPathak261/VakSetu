package com.vaksetu.topic;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vaksetu.exception.ConflictException;
import com.vaksetu.topic.dto.CreateTopicRequest;
import com.vaksetu.topic.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TopicServiceIntegrationTest {

    @Autowired
    private TopicService topicService;

    @Test
    void createTopicRejectsDuplicateTitleIgnoringCase() {
        topicService.createTopic(CreateTopicRequest.builder()
                .title("Climate Change")
                .category("Debate")
                .build());

        assertThatThrownBy(() -> topicService.createTopic(CreateTopicRequest.builder()
                .title("climate change")
                .category("Debate")
                .build()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Topic already exists");
    }
}
