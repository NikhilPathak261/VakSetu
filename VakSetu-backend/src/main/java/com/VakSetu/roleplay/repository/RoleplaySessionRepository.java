package com.vaksetu.roleplay.repository;

import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.roleplay.entity.RoleplaySession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleplaySessionRepository extends JpaRepository<RoleplaySession, Long> {

    List<RoleplaySession> findByStatus(SessionStatus status);
}
