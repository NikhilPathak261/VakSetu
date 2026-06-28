package com.vaksetu.gd.repository;

import com.vaksetu.gd.entity.SessionStar;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionStarRepository extends JpaRepository<SessionStar, Long> {

    Optional<SessionStar> findBySessionIdAndGiverIdAndReceiverId(
            Long sessionId,
            Long giverId,
            Long receiverId
    );

    boolean existsBySessionIdAndGiverIdAndReceiverId(
            Long sessionId,
            Long giverId,
            Long receiverId
    );

    long countBySessionIdAndReceiverId(
            Long sessionId,
            Long receiverId
    );
}
