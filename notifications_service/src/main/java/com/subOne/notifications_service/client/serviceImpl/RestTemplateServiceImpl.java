package com.subOne.notifications_service.client.serviceImpl;

import com.subOne.notifications_service.client.service.RestTemplateService;
import com.subOne.notifications_service.dto.ResponseGroupOwnerIdDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.util.UUID;
@Slf4j
@Service
public class RestTemplateServiceImpl implements RestTemplateService {
    private final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;
    private final String userServiceUri;

    public RestTemplateServiceImpl(AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager,
                                   RestTemplate restTemplate,
                                   @Value("${spring.user_service.url}") String userServiceUri) {
        this.authorizedClientManager = authorizedClientManager;
        this.restTemplate = restTemplate;
        this.userServiceUri = userServiceUri;
    }


    @Override
    @Nullable
    public UUID getOwnerIdByGroupId(Long groupId) {
        HttpEntity<Void> entity = new HttpEntity<>(generateAccessToken());
        ResponseEntity<ResponseGroupOwnerIdDto> response = restTemplate.exchange(
                userServiceUri + "groups/" + groupId + "/owner",
                HttpMethod.GET,
                entity,
                ResponseGroupOwnerIdDto.class
        );
        if(response.getStatusCode().is2xxSuccessful() && response.hasBody() && response.getBody() != null){
            return response.getBody().ownerId();
        }
        log.error("The service could not get information about the owner of the group, status code: {}, body: {}", response.getStatusCode(), response.getBody());
        return null;
    }

    private HttpHeaders generateAccessToken(){
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager
                .authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("discovery")
                        .principal("authorized-service")
                        .build());
        if(authorizedClient == null){
            throw new RuntimeException("The service could not receive the token");
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
        return httpHeaders;
    }
}
