package com.vaksetu.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaksetu.roleplay.repository.RoleplayScenarioRepository;
import com.vaksetu.topic.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DemoDataSeederIntegrationTest {

    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private RoleplayScenarioRepository roleplayScenarioRepository;

    @Test
    void seedsDemoDataIdempotently() {
        ApplicationArguments args = new DefaultApplicationArguments();
        long topicsAfterStartup = topicRepository.count();
        long scenariosAfterStartup = roleplayScenarioRepository.count();

        demoDataSeeder.run(args);
        demoDataSeeder.run(args);

        assertThat(topicRepository.count()).isEqualTo(topicsAfterStartup);
        assertThat(roleplayScenarioRepository.count()).isEqualTo(scenariosAfterStartup);
        assertThat(topicRepository.findByActiveTrue()).hasSizeGreaterThanOrEqualTo(6);
        assertThat(roleplayScenarioRepository.findByActiveTrue()).hasSizeGreaterThanOrEqualTo(6);
    }
}
