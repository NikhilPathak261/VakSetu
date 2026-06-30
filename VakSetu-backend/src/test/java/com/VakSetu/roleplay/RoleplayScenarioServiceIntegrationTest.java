package com.vaksetu.roleplay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vaksetu.common.enums.DifficultyLevel;
import com.vaksetu.exception.ConflictException;
import com.vaksetu.roleplay.dto.CreateRoleplayScenarioRequest;
import com.vaksetu.roleplay.dto.RoleplayScenarioResponse;
import com.vaksetu.roleplay.service.RoleplayScenarioService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoleplayScenarioServiceIntegrationTest {

    @Autowired
    private RoleplayScenarioService roleplayScenarioService;

    @Test
    void createsListsAndDeactivatesScenariosWithoutEntityExposure() {
        RoleplayScenarioResponse created = roleplayScenarioService.createScenario(CreateRoleplayScenarioRequest.builder()
                .title("Difficult stakeholder update")
                .description("A project owner explains an unexpected delay to a stakeholder.")
                .roleA("Project Owner")
                .roleB("Stakeholder")
                .difficulty(DifficultyLevel.MEDIUM)
                .build());

        List<RoleplayScenarioResponse> activeScenarios = roleplayScenarioService.getActiveScenarios();
        assertThat(activeScenarios).extracting(RoleplayScenarioResponse::getId).contains(created.getId());

        roleplayScenarioService.deactivateScenario(created.getId());

        assertThat(roleplayScenarioService.getActiveScenarios())
                .extracting(RoleplayScenarioResponse::getId)
                .doesNotContain(created.getId());
    }

    @Test
    void createScenarioRejectsDuplicateTitleIgnoringCase() {
        roleplayScenarioService.createScenario(CreateRoleplayScenarioRequest.builder()
                .title("Escalation Call")
                .description("A user asks to escalate a support issue.")
                .roleA("Customer")
                .roleB("Support Lead")
                .difficulty(DifficultyLevel.HARD)
                .build());

        assertThatThrownBy(() -> roleplayScenarioService.createScenario(CreateRoleplayScenarioRequest.builder()
                .title("escalation call")
                .description("Duplicate title")
                .roleA("Customer")
                .roleB("Support Lead")
                .difficulty(DifficultyLevel.HARD)
                .build()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Roleplay scenario already exists");
    }
}
