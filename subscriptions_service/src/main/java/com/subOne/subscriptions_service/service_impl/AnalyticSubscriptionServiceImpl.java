package com.subOne.subscriptions_service.service_impl;

import com.subOne.subscriptions_service.cache.CacheService;
import com.subOne.subscriptions_service.client.WebClientService;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticPaymentSubscriptionsDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionGroupDto;
import com.subOne.subscriptions_service.entity.AnalyticSubscription;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.AnalyticSubscriptionRepository;
import com.subOne.subscriptions_service.service.AnalyticSubscriptionService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAmount;
import java.util.NoSuchElementException;

@Service
public class AnalyticSubscriptionServiceImpl implements AnalyticSubscriptionService {

    private final AnalyticSubscriptionRepository analyticSubscriptionRepository;

    private final Integer pageSize;

    private final WebClientService webClientService;

    private final CacheService cacheService;

    public AnalyticSubscriptionServiceImpl(AnalyticSubscriptionRepository analyticSubscriptionRepository,
                                           @Value("${spring.page.size}") Integer pageSize,
                                           WebClientService webClientService, CacheService cacheService) {
        this.analyticSubscriptionRepository = analyticSubscriptionRepository;
        this.pageSize = pageSize;
        this.webClientService = webClientService;
        this.cacheService = cacheService;
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseTotalAnalyticSubscriptionDto> getTotalAnalyticById(Long groupId, Long subscriptionId, Jwt jwt) {
        return webClientService.checkUserInGroup(groupId, jwt)
                .then(Mono.defer(() -> cacheService.getValue("ANALYTIC_SUBSCRIPTION::" + subscriptionId, ResponseTotalAnalyticSubscriptionDto.class)))
                .switchIfEmpty(analyticSubscriptionRepository.selectSumAmountAndLastDateBySubscriptionId(subscriptionId)
                        .flatMap(dto -> cacheService
                                .saveValue("ANALYTIC_SUBSCRIPTION::" + subscriptionId, dto, Duration.ofMinutes(30))
                                .thenReturn(dto)
                        )
                        .switchIfEmpty(Mono.error(new NoSuchElementException("Information not found")))
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseAnalyticPaymentSubscriptionsDto> getPaymentInfoSubscriptionById(Long groupId, Long subscriptionId, Integer page, Jwt jwt) {
        return webClientService.checkUserIsOwnerGroup(groupId, jwt)
                .then(analyticSubscriptionRepository.findBySubscriptionId(subscriptionId, PageRequest.of(page, pageSize))
                .collectList()
                .map(ResponseAnalyticPaymentSubscriptionsDto::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseTotalAnalyticSubscriptionGroupDto> getAlreadyPaidByGroupId(Long groupId, Jwt jwt) {
        return webClientService.checkUserInGroup(groupId, jwt)
                .then(Mono.defer(() -> cacheService.getValue("ANALYTIC_GROUP::" + groupId, ResponseTotalAnalyticSubscriptionGroupDto.class)))
                .switchIfEmpty(analyticSubscriptionRepository.selectTotalAnalyticByGroupId(groupId)
                            .flatMap(dto -> {
                                if(dto.approxMonthPaid() == null || dto.totalAmount() == null){
                                    return Mono.error(new NoSuchElementException("Information is not found"));
                                }
                                return Mono.just(new ResponseTotalAnalyticSubscriptionGroupDto(
                                        dto.totalAmount(),
                                        dto.approxMonthPaid(),
                                        dto.approxMonthPaid().multiply(BigDecimal.valueOf(12))
                                        ));
                            }).flatMap(dto -> cacheService.saveValue("ANALYTIC_GROUP::" + groupId, dto, Duration.ofMinutes(30)).thenReturn(dto))
                );
    }

    @Override
    @Transactional
    public Mono<Void> generateAnalyticSubscription(UserSubscription userSubscription) {
        LocalDate date = userSubscription.getStartDate();
        TemporalAmount step = PaymentPeriod.valueOf(userSubscription.getPaymentPeriod()).generatePeriod();
        LocalDate now = LocalDate.now();
        return Flux.generate(() -> date,
                (dateNext, sink) -> {
                    if(dateNext.isAfter(now)){
                        sink.complete();
                    }
                    AnalyticSubscription analyticSubscription = new AnalyticSubscription();
                    analyticSubscription.setSubscriptionId(userSubscription.getId());
                    analyticSubscription.setDatePaid(dateNext);
                    analyticSubscription.setAmount(userSubscription.getAmount());
                    sink.next(analyticSubscription);
                    return dateNext.plus(step);
                }
        ).cast(AnalyticSubscription.class)
        .buffer(30)
        .concatMap(analyticSubscriptionRepository::insertAllAnalyticSubscription)
        .then();
    }

}
