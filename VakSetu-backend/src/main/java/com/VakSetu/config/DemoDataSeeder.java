package com.vaksetu.config;

import com.vaksetu.common.enums.DifficultyLevel;
import com.vaksetu.roleplay.entity.RoleplayScenario;
import com.vaksetu.roleplay.repository.RoleplayScenarioRepository;
import com.vaksetu.topic.entity.Topic;
import com.vaksetu.topic.repository.TopicRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private final TopicRepository topicRepository;
    private final RoleplayScenarioRepository roleplayScenarioRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedTopics();
        seedRoleplayScenarios();
    }

    private void seedTopics() {
        List<Topic> topics = List.of(
                topic("Artificial intelligence should be regulated", "Technology"),
                topic("Remote work is better than office work", "Workplace"),
                topic("Social media does more harm than good", "Society"),
                topic("Schools should replace exams with projects", "Education"),
                topic("Public transport should be free in cities", "Policy"),
                topic("Space exploration deserves more funding", "Science")
        );

        topics.stream()
                .filter(topic -> !topicRepository.existsByTitleIgnoreCase(topic.getTitle()))
                .forEach(topicRepository::save);
    }

    private void seedRoleplayScenarios() {
        List<RoleplayScenario> scenarios = List.of(
                scenario(
                        "Delayed refund conversation",
                        "A customer asks for help after a refund has been delayed for two weeks.",
                        "Customer",
                        "Support Manager",
                        DifficultyLevel.EASY
                ),
                scenario(
                        "Project deadline negotiation",
                        "A teammate asks to renegotiate a deadline after unexpected blockers.",
                        "Project Lead",
                        "Teammate",
                        DifficultyLevel.MEDIUM
                ),
                scenario(
                        "Client feature disagreement",
                        "A client wants a risky last-minute feature before launch.",
                        "Client",
                        "Product Consultant",
                        DifficultyLevel.HARD
                ),
                scenario(
                        "Interview self introduction",
                        "A candidate introduces their background and explains why they fit the role.",
                        "Interviewer",
                        "Candidate",
                        DifficultyLevel.EASY
                ),
                scenario(
                        "Team conflict mediation",
                        "Two team members disagree about ownership and communication responsibilities.",
                        "Team Lead",
                        "Team Member",
                        DifficultyLevel.MEDIUM
                ),
                scenario(
                        "Budget cut announcement",
                        "A manager must explain budget cuts while keeping the team motivated.",
                        "Manager",
                        "Employee",
                        DifficultyLevel.HARD
                )
        );

        scenarios.stream()
                .filter(scenario -> !roleplayScenarioRepository.existsByTitleIgnoreCase(scenario.getTitle()))
                .forEach(roleplayScenarioRepository::save);
    }

    private Topic topic(
            String title,
            String category
    ) {
        return Topic.builder()
                .title(title)
                .category(category)
                .active(true)
                .build();
    }

    private RoleplayScenario scenario(
            String title,
            String description,
            String roleA,
            String roleB,
            DifficultyLevel difficulty
    ) {
        return RoleplayScenario.builder()
                .title(title)
                .description(description)
                .roleA(roleA)
                .roleB(roleB)
                .difficulty(difficulty)
                .active(true)
                .build();
    }
}
