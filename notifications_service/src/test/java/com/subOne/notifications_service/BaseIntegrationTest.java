package com.subOne.notifications_service;

import com.subOne.notifications_service.config.TestConfig;
import com.subOne.notifications_service.config.TestContainerConfig;
import com.subOne.notifications_service.config.TestSecurityConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@Import({TestConfig.class, TestContainerConfig.class, TestSecurityConfig.class})
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {
}
