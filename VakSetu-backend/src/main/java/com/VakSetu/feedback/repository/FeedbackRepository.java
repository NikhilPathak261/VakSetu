package com.vaksetu.feedback.repository;

import com.vaksetu.feedback.entity.Feedback;
import com.vaksetu.common.enums.SessionType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findBySessionId(Long sessionId);

    List<Feedback> findBySessionIdAndSessionType(
            Long sessionId,
            SessionType sessionType
    );

    boolean existsBySessionIdAndSessionTypeAndEvaluatorIdAndTargetUserId(
            Long sessionId,
            SessionType sessionType,
            Long evaluatorId,
            Long targetUserId
    );
}
