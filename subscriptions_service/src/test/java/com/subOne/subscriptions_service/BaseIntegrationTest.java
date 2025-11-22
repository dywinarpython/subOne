package com.subOne.subscriptions_service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.subOne.subscriptions_service.config.TestConfig;
import com.subOne.subscriptions_service.config.TestContainerConfig;
import com.subOne.subscriptions_service.config.TestSecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class, RedisAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@Import({TestConfig.class, TestContainerConfig.class, TestSecurityConfig.class})
@AutoConfigureWebTestClient
public abstract class BaseIntegrationTest  {


    protected final static WireMockServer wireMockServer = new WireMockServer(0);

    static {
        wireMockServer.start();
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/api/v1/groups/\\d+/members/check"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                )
        );

        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/api/v1/groups/\\d+/members/check/owner"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                )
        );
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("user_service.url", () -> "http://localhost:" + wireMockServer.port() + "/api/v1/");
    }
}
