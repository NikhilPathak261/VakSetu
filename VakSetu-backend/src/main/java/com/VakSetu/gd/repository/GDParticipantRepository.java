package com.vaksetu.gd.repository;

import com.vaksetu.gd.entity.GDParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GDParticipantRepository extends JpaRepository<GDParticipant, Long> {

    List<GDParticipant> findBySessionId(Long sessionId);

    List<GDParticipant> findBySessionIdAndLeftAtIsNull(Long sessionId);

    Optional<GDParticipant> findBySessionIdAndUserIdAndLeftAtIsNull(
            Long sessionId,
            Long userId
    );

    Optional<GDParticipant> findFirstBySessionIdAndLeftAtIsNullOrderByJoinedAtAsc(
            Long sessionId
    );

    boolean existsBySessionIdAndUserIdAndLeftAtIsNull(
            Long sessionId,
            Long userId
    );

    long countBySessionIdAndLeftAtIsNull(Long sessionId);
}
