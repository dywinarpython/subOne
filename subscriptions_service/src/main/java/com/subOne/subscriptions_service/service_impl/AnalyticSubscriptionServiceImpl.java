package com.subOne.subscriptions_service.service_impl;

import com.subOne.subscriptions_service.client.WebClientService;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticSubscriptionDto;
import com.subOne.subscriptions_service.entity.AnalyticSubscription;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.repository.AnalyticSubscriptionRepository;
import com.subOne.subscriptions_service.service.AnalyticSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAmount;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AnalyticSubscriptionServiceImpl implements AnalyticSubscriptionService {

    private final AnalyticSubscriptionRepository analyticSubscriptionRepository;


    private final WebClientService webClientService;

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseAnalyticSubscriptionDto> getAnalyticById(Long groupId, Long subscriptionId, Jwt jwt) {
        return webClientService.checkUserInGroup(groupId, jwt)
                .then(analyticSubscriptionRepository.findBySubscriptionId(subscriptionId))
                .switchIfEmpty(Mono.error(new NoSuchElementException("Information not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BigDecimal> getAlreadyPaidByGroupId(Long groupId, Jwt jwt) {
        return webClientService.checkUserInGroup(groupId, jwt)
                .then(analyticSubscriptionRepository.findAllAlreadyPaidByGroupId(groupId))
                .switchIfEmpty(Mono.just(BigDecimal.ZERO));
    }

    @Override
    @Transactional
    // TODO разобраться с количеством запросов
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
                    sink.next(analyticSubscription);
                    return dateNext.plus(step);
                }
        ).cast(AnalyticSubscription.class)
        .buffer(20)
        .flatMap(analyticSubscriptionRepository::saveAll)
        .then();
    }

}
