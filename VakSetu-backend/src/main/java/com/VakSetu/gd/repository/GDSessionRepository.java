package com.vaksetu.gd.repository;

import com.vaksetu.common.enums.SessionStatus;
import com.vaksetu.gd.entity.GroupDiscussionSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GDSessionRepository extends JpaRepository<GroupDiscussionSession, Long> {

    List<GroupDiscussionSession> findByStatus(SessionStatus status);
}
