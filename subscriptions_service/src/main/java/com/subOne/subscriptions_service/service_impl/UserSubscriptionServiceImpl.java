package com.subOne.subscriptions_service.service_impl;

import com.subOne.subscriptions_service.cache.CacheService;
import com.subOne.subscriptions_service.client.WebClientService;
import com.subOne.subscriptions_service.dto.subscription.request.RequestSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.request.RequestUpdateSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionsDto;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.mapper.UserSubscriptionMapper;
import com.subOne.subscriptions_service.repository.user_subscription_repository.UserSubscriptionRepository;
import com.subOne.subscriptions_service.service.AnalyticSubscriptionService;
import com.subOne.subscriptions_service.service.UserSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class UserSubscriptionServiceImpl implements UserSubscriptionService {


    private final UserSubscriptionRepository userSubscriptionRepository;

    private final AnalyticSubscriptionService analyticSubscriptionService;

    private final UserSubscriptionMapper userSubscriptionMapper;

    private final WebClientService webClientService;


    private final CacheService cacheService;

    private final Integer pageSize;

    public UserSubscriptionServiceImpl(UserSubscriptionRepository userSubscriptionRepository,
                                       AnalyticSubscriptionService analyticSubscriptionService,
                                       UserSubscriptionMapper userSubscriptionMapper,
                                       WebClientService webClientService, CacheService cacheService,
                                       @Value("${spring.page.size}") Integer pageSize) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.analyticSubscriptionService = analyticSubscriptionService;
        this.userSubscriptionMapper = userSubscriptionMapper;
        this.webClientService = webClientService;
        this.cacheService = cacheService;
        this.pageSize = pageSize;
    }


    @Override
    @Transactional
    public Mono<ResponseSubscriptionDto> saveSubscription(Long groupId, Mono<RequestSubscriptionDto> requestSubscriptionDtoMono, Jwt jwt) {
        return requestSubscriptionDtoMono.flatMap(requestSubscriptionDto -> {
            if(requestSubscriptionDto.startDate().equals(requestSubscriptionDto.endDate())) return Mono.error(new ValidationException("The start date must not be equal to the end date"));
            if (requestSubscriptionDto.startDate().isAfter(requestSubscriptionDto.endDate())) return Mono.error(new ValidationException("Start date must not be after end date"));
            if (requestSubscriptionDto.startDate().isBefore(LocalDate.now().minusYears(10))) return Mono.error(new ValidationException("The start date is too early"));
            return webClientService.checkUserIsOwnerGroup(groupId, jwt)
                    .then(userSubscriptionRepository
                                .save(userSubscriptionMapper.requestSubscriptionDtoToUserSubscription(requestSubscriptionDto, groupId)))
                    .flatMap(userSubscription -> analyticSubscriptionService.generateAnalyticSubscription(userSubscription).thenReturn(userSubscription))
                    .map(userSubscriptionMapper::userSubscriptionToResponseSubscriptionDto)
                    .flatMap(dto -> cacheService.deleteValue("ANALYTIC_GROUP::" + groupId).thenReturn(dto));
        });
    }

    @Override
    @Transactional
    public Mono<Void> updateSubscription(Long groupId, Long subscriptionId, Mono<RequestUpdateSubscriptionDto> requestUpdateSubscriptionDtoMono, Jwt jwt) {
        return requestUpdateSubscriptionDtoMono.flatMap( requestUpdateSubscriptionDto -> {
            Map<SqlIdentifier, Object> updateMap = userSubscriptionMapper.addUpdateField(requestUpdateSubscriptionDto);
            if (updateMap.isEmpty())
                return Mono.error(new ValidationException("Not a single field has been transmitted"));
            updateMap.put(SqlIdentifier.quoted("updated_at"), OffsetDateTime.now());
            return webClientService.checkUserIsOwnerGroup(groupId, jwt)
                    .then(userSubscriptionRepository
                                .updateFieldsByField(updateMap, UserSubscription.class, "id", subscriptionId))
                    .flatMap(count -> count == 0 ? Mono.error(new NoSuchElementException("Subscription is not found")) : Mono.empty());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseSubscriptionsDto> getSubscriptionsGroup(Long groupId, Integer page, Jwt jwt) {
        return webClientService.checkUserInGroup(groupId, jwt)
                .thenMany(userSubscriptionRepository.findByGroupId(groupId, PageRequest.of(page, pageSize)))
                .collectList()
                .map(ResponseSubscriptionsDto::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseSubscriptionDto> getSubscriptionById(Long groupId, Long subscriptionId, Jwt jwt) {
        return webClientService.checkUserInGroup(groupId, jwt)
                .then(userSubscriptionRepository.findBySubscriptionId(subscriptionId))
                .switchIfEmpty(Mono.error(new NoSuchElementException("Subscription is not found")));
    }

    @Override
    @Transactional
    public Mono<Void> deleteSubscriptionById(Long groupId, Long subscriptionId, Jwt jwt) {
        return webClientService.checkUserIsOwnerGroup(groupId, jwt)
                .then(userSubscriptionRepository.deleteByIdReturningCount(subscriptionId))
                .flatMap(count -> count == 0? Mono.error(new NoSuchElementException("Subscription is not found")): Mono.empty());
    }

    @Override
    @Transactional
    public Mono<Void> deleteSubscriptionsByGroupId(Long groupId) {
        return userSubscriptionRepository.deleteByGroupId(groupId).flatMap(count -> {
            if(count == 0) return Mono.error(new NoSuchElementException("Subscriptions is not found!"));
            return Mono.empty();
        });
    }

    @Override
    @Transactional
    public Mono<Void> renewSubscriptionById(Long groupId, Long subscriptionId, Long extensionCount, Jwt jwt) {
        return Mono.just(extensionCount)
                .flatMap(ex -> ex > 0? Mono.empty(): Mono.error(new ValidationException("The number of extensions is less than 0")))
                .then(Mono.defer(() -> webClientService.checkUserIsOwnerGroup(groupId, jwt)))
                .then(Mono.defer( () -> userSubscriptionRepository.updateEndTimeSubscriptionById(subscriptionId, extensionCount)))
                .flatMap(count -> count == 1? Mono.empty(): Mono.error(new NoSuchElementException("Subscription is not found")));
    }
}
