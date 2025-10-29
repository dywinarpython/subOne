package com.subOne.subscriptions_service;

import com.subOne.subscriptions_service.config.TestConfig;
import com.subOne.subscriptions_service.config.TestContainerConfig;
import com.subOne.subscriptions_service.config.TestSecurityConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class, RedisAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@Import({TestConfig.class, TestContainerConfig.class, TestSecurityConfig.class})
@AutoConfigureWebTestClient
public abstract class BaseIntegrationTest  {
}
