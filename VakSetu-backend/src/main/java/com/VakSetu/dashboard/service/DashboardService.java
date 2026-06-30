package com.vaksetu.dashboard.service;

import com.vaksetu.common.mapper.DashboardMapper;
import com.vaksetu.dashboard.dto.DashboardSummaryResponse;
import com.vaksetu.dashboard.dto.ReputationHistoryResponse;
import com.vaksetu.dashboard.dto.SkillHistoryResponse;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.reputation.repository.ReputationHistoryRepository;
import com.vaksetu.skill.repository.SkillHistoryRepository;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.user.repository.UserSkillRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillHistoryRepository skillHistoryRepository;
    private final ReputationHistoryRepository reputationHistoryRepository;

    public DashboardSummaryResponse getSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserSkill userSkill = userSkillRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));

        return DashboardMapper.toSummaryResponse(user, userSkill);
    }

    public List<SkillHistoryResponse> getSkillHistory(Long userId) {
        ensureUserExists(userId);

        return skillHistoryRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(DashboardMapper::toSkillHistoryResponse)
                .toList();
    }

    public List<ReputationHistoryResponse> getReputationHistory(Long userId) {
        ensureUserExists(userId);

        return reputationHistoryRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(DashboardMapper::toReputationHistoryResponse)
                .toList();
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
    }

}
