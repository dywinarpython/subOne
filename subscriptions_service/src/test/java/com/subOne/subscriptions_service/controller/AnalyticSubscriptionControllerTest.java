package com.subOne.subscriptions_service.controller;

import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticPaymentSubscriptionsDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionGroupDto;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.AnalyticSubscriptionRepository;
import com.subOne.subscriptions_service.service.AnalyticSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyticSubscriptionControllerTest extends UserSubscriptionControllerTest{


    @Autowired
    private AnalyticSubscriptionRepository analyticSubscriptionRepository;

    @Autowired
    private AnalyticSubscriptionService analyticSubscriptionService;


    private void checkAnalytic(ResponseTotalAnalyticSubscriptionGroupDto analyticGroup,
                               ResponseTotalAnalyticSubscriptionGroupDto resultRepo){
        assertEquals(0, analyticGroup.approxMonthPaid().compareTo(resultRepo.approxMonthPaid()));
        assertEquals(0, analyticGroup.approxYearPaid().compareTo(resultRepo.approxMonthPaid().multiply(BigDecimal.valueOf(12))));
        assertEquals(0, analyticGroup.totalAmount().compareTo(resultRepo.totalAmount()));
    }

    @BeforeEach
    void setUp(){
        super.setUp();
        analyticSubscriptionService.generateAnalyticSubscription(userSubscription).block();
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/subscriptions/{subscriptionId}/analytic")
    void getAnalyticSubscriptionById_GroupFoundAndSubscriptionFound_CorrectReturnAndCheckCache(){
        Flux<ResponseTotalAnalyticSubscriptionDto> result = webTestClient.get()
                .uri(URI + "/" + userSubscription.getId() + "/analytic")
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseTotalAnalyticSubscriptionDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(dto -> {
                    StepVerifier.create(analyticSubscriptionRepository.selectSumAmountAndLastDateBySubscriptionId(userSubscription.getId()))
                            .expectNext(dto)
                            .verifyComplete();
                    StepVerifier.create(cacheService.getValue("ANALYTIC_SUBSCRIPTION::" + userSubscription.getId(),
                                    ResponseTotalAnalyticSubscriptionDto.class))
                            .expectNextCount(1)
                            .expectNext(dto);
                }).verifyComplete();
    }
    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/subscriptions/{subscriptionId}/payment-info?page=")
    void getPaymentInfoSubscriptionById_GroupFoundAndSubscriptionFound_CorrectReturn(){
        Flux<ResponseAnalyticPaymentSubscriptionsDto> result = webTestClient.get()
                .uri(URI + "/" + userSubscription.getId() + "/payment-info?page=" + 0)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseAnalyticPaymentSubscriptionsDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext( paymentsInfo -> StepVerifier.create(analyticSubscriptionRepository.findBySubscriptionId(
                        userSubscription.getId(), PageRequest.of(0, 10))
                                .collectList().map(ResponseAnalyticPaymentSubscriptionsDto::new))
                        .expectNext(paymentsInfo)
                        .verifyComplete()).verifyComplete();
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/subscriptions/analytic/group-summary")
    void getPaymentInfoSubscriptionById_GroupFound_CorrectReturnAndCacheCheck(){
        Flux<ResponseTotalAnalyticSubscriptionGroupDto> result = webTestClient.get()
                .uri(URI + "/analytic/group-summary")
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseTotalAnalyticSubscriptionGroupDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext( analyticGroup -> StepVerifier.create(cacheService.getValue("ANALYTIC_GROUP::" + userSubscription.getGroupId(), ResponseTotalAnalyticSubscriptionGroupDto.class))
                        .assertNext(resultRepo -> this.checkAnalytic(analyticGroup, resultRepo))
                        .verifyComplete()).verifyComplete();
    }



}
