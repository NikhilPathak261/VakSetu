package com.vaksetu.reputation.repository;

import com.vaksetu.reputation.entity.ReputationHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReputationHistoryRepository extends JpaRepository<ReputationHistory, Long> {

    List<ReputationHistory> findByUserId(Long userId);
}
