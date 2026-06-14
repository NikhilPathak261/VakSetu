package com.vaksetu.gd.repository;

import com.vaksetu.gd.entity.SessionStar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionStarRepository extends JpaRepository<SessionStar, Long> {

    boolean existsBySessionIdAndGiverIdAndReceiverId(
            Long sessionId,
            Long giverId,
            Long receiverId
    );
}
