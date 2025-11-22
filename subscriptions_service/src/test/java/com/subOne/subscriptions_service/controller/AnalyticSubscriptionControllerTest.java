package com.subOne.subscriptions_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticPaymentSubscriptionsDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticGroupsDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionGroupDto;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.AnalyticSubscriptionRepository;
import com.subOne.subscriptions_service.service.AnalyticSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class AnalyticSubscriptionControllerTest extends UserSubscriptionControllerTest{

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("GET -> /api/v1/groups/me/analytic")
    void getTotalAnalyticGroups_GroupsFound_CorrectReturn() throws JsonProcessingException {

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/groups/owner/me/id"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(List.of(userSubscription.getGroupId())))
                )
        );
        Flux<ResponseTotalAnalyticGroupsDto> result = webTestClient.get()
                .uri("/api/v1/groups/me/analytic")
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseTotalAnalyticGroupsDto.class).getResponseBody();

        StepVerifier.create(analyticSubscriptionRepository.
                selectTotalAnalyticByGroupsId(List.of(userSubscription.getGroupId())))
                .assertNext(responseTotalAnalyticGroupsDto -> {
                   StepVerifier.create(result)
                           .assertNext(dto -> assertEquals(responseTotalAnalyticGroupsDto, dto));
                }).verifyComplete();
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

        await().atMost(5, TimeUnit.SECONDS).untilAsserted( () -> StepVerifier.create(result)
                .assertNext( analyticGroup -> StepVerifier.create(cacheService.getValue("ANALYTIC_GROUP::" + userSubscription.getGroupId(), ResponseTotalAnalyticSubscriptionGroupDto.class))
                        .expectNextCount(1)
                        .verifyComplete()).verifyComplete());
    }



}
