package com.vaksetu.roleplay.service;

import com.vaksetu.common.mapper.RoleplayMapper;
import com.vaksetu.exception.ConflictException;
import com.vaksetu.exception.ResourceNotFoundException;
import com.vaksetu.roleplay.dto.CreateRoleplayScenarioRequest;
import com.vaksetu.roleplay.dto.RoleplayScenarioResponse;
import com.vaksetu.roleplay.entity.RoleplayScenario;
import com.vaksetu.roleplay.repository.RoleplayScenarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleplayScenarioService {

    private final RoleplayScenarioRepository roleplayScenarioRepository;

    public List<RoleplayScenarioResponse> getActiveScenarios() {
        return roleplayScenarioRepository.findByActiveTrue()
                .stream()
                .map(RoleplayMapper::toScenarioResponse)
                .toList();
    }

    @Transactional
    public RoleplayScenarioResponse createScenario(CreateRoleplayScenarioRequest request) {
        if (roleplayScenarioRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new ConflictException("Roleplay scenario already exists");
        }

        RoleplayScenario scenario = RoleplayScenario.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .roleA(request.getRoleA())
                .roleB(request.getRoleB())
                .difficulty(request.getDifficulty())
                .active(true)
                .build();

        return RoleplayMapper.toScenarioResponse(roleplayScenarioRepository.save(scenario));
    }

    @Transactional
    public void deactivateScenario(Long scenarioId) {
        RoleplayScenario scenario = roleplayScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Roleplay scenario not found"));

        scenario.setActive(false);
    }
}
