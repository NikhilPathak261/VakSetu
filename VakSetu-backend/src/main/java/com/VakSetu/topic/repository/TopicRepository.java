package com.vaksetu.topic.repository;

import com.vaksetu.topic.entity.Topic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByActiveTrue();

    boolean existsByTitleIgnoreCase(String title);
}
