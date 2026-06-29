package com.vaksetu.roleplay.repository;

import com.vaksetu.common.enums.DifficultyLevel;
import com.vaksetu.roleplay.entity.RoleplayScenario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleplayScenarioRepository extends JpaRepository<RoleplayScenario, Long> {

    List<RoleplayScenario> findByDifficulty(DifficultyLevel difficulty);

    List<RoleplayScenario> findByActiveTrue();

    boolean existsByTitleIgnoreCase(String title);
}
