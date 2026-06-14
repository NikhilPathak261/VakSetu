package com.vaksetu.skill.repository;

import com.vaksetu.skill.entity.SkillHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillHistoryRepository extends JpaRepository<SkillHistory, Long> {

    List<SkillHistory> findByUserId(Long userId);
}
