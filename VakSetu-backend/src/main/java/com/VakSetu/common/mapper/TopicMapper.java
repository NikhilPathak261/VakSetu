package com.vaksetu.common.mapper;

import com.vaksetu.topic.dto.TopicResponse;
import com.vaksetu.topic.entity.Topic;

public final class TopicMapper {

    private TopicMapper() {
    }

    public static TopicResponse toResponse(Topic topic) {
        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .category(topic.getCategory())
                .active(topic.getActive())
                .build();
    }
}
