package com.vaksetu.feedback.repository;

import com.vaksetu.feedback.entity.Feedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findBySessionId(Long sessionId);
}
