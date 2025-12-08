package com.subOne.subscriptions_service.client;

import com.subOne.subscriptions_service.cache.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
public class WebClientServiceImpl implements WebClientService {
    private final WebClient webClient;
    private final Integer maxReties;
    private final CacheService cacheService;

    public WebClientServiceImpl(WebClient webClient,
                                @Value("${user_service.max_retries:5}") Integer maxReties, CacheService cacheService) {
        this.webClient = webClient;
        this.maxReties = maxReties;
        this.cacheService = cacheService;
    }

    @Override
    public Mono<Void> checkUserInGroup(Integer groupId, Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String memberKey = "MEMBER::" + userId + ' ' + groupId;
        String ownerKey = "OWNER::" + groupId;
        return cacheService.getValue(memberKey, Boolean.class)
                .switchIfEmpty(
                        cacheService.getValue(ownerKey, UUID.class)
                                .map(ownerId -> ownerId.equals(userId))
                                .switchIfEmpty(Mono.just(false))
                ).flatMap(bl -> {
                    if(bl) return Mono.empty();
                    return requestProcessing(webClient.get()
                                .uri("groups/" + groupId + "/members/check")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                                .retrieve(), Void.class);
                });
    }

    @Override
    public Mono<Void> checkUserIsOwnerGroup(Integer groupId, Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String ownerKey = "OWNER::" + groupId;
        return cacheService.getValue(ownerKey, UUID.class)
                .map(ownerId -> ownerId.equals(userId))
                .switchIfEmpty(Mono.just(false))
                .flatMap(bl -> {
                    if(bl) return Mono.empty();
                    return requestProcessing(webClient.get()
                            .uri( "groups/" + groupId + "/members/check/owner")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                            .retrieve(), Void.class);
                });
    }

    @Override
    public Mono<List<Integer>> getGroupsIdByOwnerId(Jwt jwt) {
        return requestProcessingList(webClient.get()
                .uri("groups/owner/me/id")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                .retrieve(), Integer.class);
    }


    private <T> Mono<T> requestProcessing(WebClient.ResponseSpec responseSpec, Class<T> classz){
        return responseSpec(responseSpec)
                .bodyToMono(classz)
                .retryWhen(retrySpec());
    }

    private <T> Mono<List<T>> requestProcessingList(WebClient.ResponseSpec responseSpec, Class<T> classz){
        return responseSpec(responseSpec)
                .bodyToFlux(classz)
                .retryWhen(retrySpec())
                .collectList();
    }


    private WebClient.ResponseSpec responseSpec(WebClient.ResponseSpec responseSpec){
        return responseSpec
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    HttpStatusCode status = clientResponse.statusCode();
                    return switch (status) {
                        case HttpStatus.FORBIDDEN -> Mono.error(new AccessDeniedException("Access is denied"));
                        case HttpStatus.NOT_FOUND -> clientResponse.bodyToMono(Map.class)
                                .flatMap(message -> Mono.error(new NoSuchElementException(message.get("warn").toString())));
                        case HttpStatus.UNAUTHORIZED -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized"));
                        default -> Mono.error(new Exception("Client exception: " + clientResponse));
                    };
        });
    }

    private Retry retrySpec(){
        return Retry.backoff(maxReties, Duration.ofSeconds(3))
                .filter(throwable -> throwable instanceof WebClientRequestException)
                .doBeforeRetry(retrySignal -> log.warn("Retry: {}, error: {}", retrySignal.totalRetries() + 1, retrySignal.failure().getMessage()))
                .onRetryExhaustedThrow((spec, signal) -> {
                    log.error("All attempts were exhausted after {} repetitions", spec.maxAttempts);
                    log.error("Connection error: {}", signal.failure().getMessage(), signal.failure());
                    return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The service is temporarily unavailable");
                });
    }
}
