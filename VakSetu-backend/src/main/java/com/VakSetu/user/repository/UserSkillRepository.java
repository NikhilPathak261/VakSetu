package com.vaksetu.user.repository;

import com.vaksetu.user.entity.UserSkill;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    Optional<UserSkill> findByUserId(Long userId);
}
