package com.vaksetu.debate.repository;

import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.debate.entity.DebateSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebateSessionRepository extends JpaRepository<DebateSession, Long> {

    List<DebateSession> findByStatus(SessionStatus status);
}
