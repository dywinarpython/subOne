package com.subOne.subscriptions_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder,
                               @Value("${user_service.url}") String userServiceUrl) {
        log.info("URI userService: {}", userServiceUrl);
        return builder.baseUrl(userServiceUrl).build();
    }
}
