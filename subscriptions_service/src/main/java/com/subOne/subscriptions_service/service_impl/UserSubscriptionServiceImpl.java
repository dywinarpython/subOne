package com.subOne.subscriptions_service.service_impl;

import com.subOne.subscriptions_service.client.WebClientService;
import com.subOne.subscriptions_service.dto.request.RequestSubscriptionDto;
import com.subOne.subscriptions_service.dto.request.RequestUpdateSubscriptionDto;
import com.subOne.subscriptions_service.dto.response.ResponseSubscriptionDto;
import com.subOne.subscriptions_service.dto.response.ResponseSubscriptionsDto;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.mapper.UserSubscriptionMapper;
import com.subOne.subscriptions_service.repository.UserSubscriptionRepository;
import com.subOne.subscriptions_service.service.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import javax.xml.bind.ValidationException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;

    private final UserSubscriptionMapper userSubscriptionMapper;

    private final WebClientService webClientService;


    @Override
    @Transactional
    public Mono<ResponseSubscriptionDto> saveSubscription(Long groupId, Mono<RequestSubscriptionDto> requestSubscriptionDtoMono, Jwt jwt) {
        return requestSubscriptionDtoMono.flatMap(requestSubscriptionDto -> {
            if (requestSubscriptionDto.startDate().isAfter(requestSubscriptionDto.endDate())) return Mono.error(new ValidationException("Start date must not be after end date"));
            return webClientService.checkUserIsOwnerGroup(groupId, jwt)
                        .then(userSubscriptionRepository
                                .save(userSubscriptionMapper.requestSubscriptionDtoToUserSubscription(requestSubscriptionDto, groupId)))
                        .map(userSubscriptionMapper::userSubscriptionToResponseSubscriptionDto);
        });
    }

    @Override
    @Transactional
    public Mono<Void> updateSubscription(Long groupId, Long subscriptionId, Mono<RequestUpdateSubscriptionDto> requestUpdateSubscriptionDtoMono, Jwt jwt) {
        return requestUpdateSubscriptionDtoMono.flatMap( requestUpdateSubscriptionDto -> {
            if(requestUpdateSubscriptionDto.startDate() != null && requestUpdateSubscriptionDto.endDate() != null) {
                if(requestUpdateSubscriptionDto.startDate().isAfter(requestUpdateSubscriptionDto.endDate())) return Mono.error(new ValidationException("Start date must not be after end date"));
            }
            Map<SqlIdentifier, Object> updateMap = userSubscriptionMapper.addUpdateField(requestUpdateSubscriptionDto);
            if (updateMap.isEmpty())
                return Mono.error(new ValidationException("Not a single field has been transmitted"));
            updateMap.put(SqlIdentifier.quoted("updated_at"), OffsetDateTime.now());
            return webClientService.checkUserIsOwnerGroup(groupId, jwt)
                    .then(userSubscriptionRepository
                                .updateFields(updateMap, UserSubscription.class, "id", subscriptionId))
                    .flatMap(count -> count == 0 ? Mono.error(new NoSuchElementException("Subscription is not found")) : Mono.empty());
        });
    }

    @Override
    @Transactional(readOnly = true)
    // TODO добавить расчет суммы сколько уже потрачено и тп
    public Mono<ResponseSubscriptionsDto> getSubscriptionsGroup(Long groupId, Jwt jwt) {
        return webClientService.checkUserInGroup(groupId, jwt)
                .thenMany(userSubscriptionRepository.findByGroupId(groupId))
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
                .then(userSubscriptionRepository.deleteById(subscriptionId));
    }
}
