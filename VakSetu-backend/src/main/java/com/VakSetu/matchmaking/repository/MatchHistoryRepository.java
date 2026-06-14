package com.vaksetu.matchmaking.repository;

import com.vaksetu.matchmaking.entity.MatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchHistoryRepository extends JpaRepository<MatchHistory, Long> {
}
