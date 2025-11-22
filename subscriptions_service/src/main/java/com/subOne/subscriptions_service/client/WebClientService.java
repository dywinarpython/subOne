package com.subOne.subscriptions_service.client;

import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WebClientService {
    Mono<Void> checkUserInGroup(Long groupId, Jwt jwt);
    Mono<Void> checkUserIsOwnerGroup(Long groupId, Jwt jwt);
    Mono<List<Long>> getGroupsIdByOwnerId(Jwt jwt);
}
