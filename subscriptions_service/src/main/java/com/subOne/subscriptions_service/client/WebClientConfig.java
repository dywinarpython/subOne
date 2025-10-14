package com.subOne.subscriptions_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder,
                               @Value("${spring.user_service.url}") String userServiceUrl){
        return builder
                .baseUrl(userServiceUrl)
                .build();
    }
}
