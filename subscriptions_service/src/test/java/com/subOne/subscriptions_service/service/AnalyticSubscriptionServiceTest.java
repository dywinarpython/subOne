package com.subOne.subscriptions_service.service;

import com.subOne.subscriptions_service.cache.CacheService;
import com.subOne.subscriptions_service.client.WebClientService;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.*;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.AnalyticSubscriptionRepository;
import com.subOne.subscriptions_service.service_impl.AnalyticSubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AnalyticSubscriptionServiceTest {

    @Mock
    private AnalyticSubscriptionRepository analyticSubscriptionRepository;

    @Mock
    private WebClientService webClientService;

    @Mock
    private CacheService cacheService;

    private AnalyticSubscriptionServiceImpl analyticSubscriptionService;


    @BeforeEach
    void setUp() {
        analyticSubscriptionService = new AnalyticSubscriptionServiceImpl(
                analyticSubscriptionRepository,
                10,
                webClientService,
                cacheService
        );
    }


    @Test
    @DisplayName("getTotalAnalyticById -> found: group,subscription, not found -> cache")
    void getTotalAnalyticById_GroupFoundAndSubscriptionFoundAndCacheIsNotFound_CorrectReturnAndCheckRepo(){
        ResponseTotalAnalyticSubscriptionDto responseTotalAnalyticSubscriptionDto = new ResponseTotalAnalyticSubscriptionDto(
                BigDecimal.valueOf(1000),
                LocalDate.now()
        );
        when(webClientService.checkUserInGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());
        when(analyticSubscriptionRepository.selectSumAmountAndLastDateBySubscriptionId(anyLong()))
                .thenReturn(Mono.just(responseTotalAnalyticSubscriptionDto));
        when(cacheService.saveValue(anyString(), any(), any())).thenReturn(Mono.empty());

        Mono<ResponseTotalAnalyticSubscriptionDto> result = analyticSubscriptionService.getTotalAnalyticById(1L, 1L, null);


        StepVerifier.create(result)
                .expectNext(responseTotalAnalyticSubscriptionDto)
                .verifyComplete();
        verify(webClientService).checkUserInGroup(anyLong(), any());
        verify(cacheService).getValue(any(), any());
        verify(analyticSubscriptionRepository).selectSumAmountAndLastDateBySubscriptionId(anyLong());
        verify(cacheService).saveValue(anyString(), any(), any());
    }



    @Test
    @DisplayName("getTotalAnalyticById -> not found: group,subscription")
    void getTotalAnalyticById_GroupFoundAndSubscriptionNotFoundAndCacheIsNotFound_NotCorrectReturnAndCheckRepo(){
        when(webClientService.checkUserInGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());
        when(analyticSubscriptionRepository.selectSumAmountAndLastDateBySubscriptionId(anyLong()))
                .thenReturn(Mono.empty());

        Mono<ResponseTotalAnalyticSubscriptionDto> result = analyticSubscriptionService.getTotalAnalyticById(1L, 1L, null);


        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(webClientService).checkUserInGroup(anyLong(), any());
        verify(cacheService).getValue(any(), any());
        verify(analyticSubscriptionRepository).selectSumAmountAndLastDateBySubscriptionId(anyLong());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
    }

    @Test
    @DisplayName("getPaymentInfoSubscriptionById -> found: group, subscription")
    void getPaymentInfoSubscriptionById_GroupFoundAndSubscriptionFound_CorrectReturnAndCheckRepo(){
        List<ResponseAnalyticPaymentSubscriptionDto> ls = List.of(
                new ResponseAnalyticPaymentSubscriptionDto(LocalDate.now(), BigDecimal.valueOf(1000)),
                new ResponseAnalyticPaymentSubscriptionDto(LocalDate.now().minusMonths(1), BigDecimal.valueOf(1000)),
                new ResponseAnalyticPaymentSubscriptionDto(LocalDate.now().minusMonths(2), BigDecimal.valueOf(1000))
        );
        Flux<ResponseAnalyticPaymentSubscriptionDto>  fluxLs = Flux.fromIterable(
                List.of(
                       new ResponseAnalyticPaymentSubscriptionDto(LocalDate.now(), BigDecimal.valueOf(1000)),
                       new ResponseAnalyticPaymentSubscriptionDto(LocalDate.now().minusMonths(1), BigDecimal.valueOf(1000)),
                       new ResponseAnalyticPaymentSubscriptionDto(LocalDate.now().minusMonths(2), BigDecimal.valueOf(1000))
                )
        );
        when(webClientService.checkUserInGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(analyticSubscriptionRepository.findBySubscriptionId(anyLong(), any())).thenReturn(fluxLs);

        Mono<ResponseAnalyticPaymentSubscriptionsDto> result = analyticSubscriptionService.getPaymentInfoSubscriptionById(1L, 1L, 0, null);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    List<ResponseAnalyticPaymentSubscriptionDto> dtoLs = dto.paymentsInfo();
                    assertEquals(ls.size(), dtoLs.size());
                });
        verify(webClientService).checkUserInGroup(anyLong(), any());
        verify(analyticSubscriptionRepository).findBySubscriptionId(anyLong(), any());

    }

    @Test
    @DisplayName("getPaymentInfoSubscriptionById -> found: group, not found -> subscription")
    void getPaymentInfoSubscriptionById_GroupFoundAndSubscriptionNotFound_CorrectReturnAndCheckRepo(){
        when(webClientService.checkUserInGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(analyticSubscriptionRepository.findBySubscriptionId(anyLong(), any(PageRequest.class))).thenReturn(Flux.empty());

        Mono<ResponseAnalyticPaymentSubscriptionsDto> result = analyticSubscriptionService.getPaymentInfoSubscriptionById(1L, 1L, 0, null);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    List<ResponseAnalyticPaymentSubscriptionDto> dtoLs = dto.paymentsInfo();
                    assertEquals(0, dtoLs.size());
                });
        verify(webClientService).checkUserInGroup(anyLong(), any());
        verify(analyticSubscriptionRepository).findBySubscriptionId(anyLong(), any());
    }

    @Test
    void getAlreadyPaidByGroupId_GroupFoundAndCacheNotFound_CorrectReturnAndCheckRepo(){
        ResponseTotalAnalyticSubscriptionGroupDto responseTotalAnalyticSubscriptionGroupDto = new ResponseTotalAnalyticSubscriptionGroupDto(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                null
        );
        when(webClientService.checkUserInGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());
        when(analyticSubscriptionRepository.selectTotalAnalyticByGroupId(anyLong())).thenReturn(Mono.just(responseTotalAnalyticSubscriptionGroupDto));
        when(cacheService.saveValue(anyString(), any(), any())).thenReturn(Mono.empty());

        Mono<ResponseTotalAnalyticSubscriptionGroupDto> result = analyticSubscriptionService.getAlreadyPaidByGroupId(anyLong(), any());

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertEquals(responseTotalAnalyticSubscriptionGroupDto.approxMonthPaid(), dto.approxMonthPaid());
                    assertEquals(responseTotalAnalyticSubscriptionGroupDto.totalAmount(), dto.totalAmount());
                    assertEquals(
                            responseTotalAnalyticSubscriptionGroupDto.approxMonthPaid().multiply(BigDecimal.valueOf(12)),
                            dto.approxYearPaid()
                    );
                }).verifyComplete();
        verify(webClientService).checkUserInGroup(anyLong(), any());
        verify(cacheService).getValue(any(), any());
        verify(analyticSubscriptionRepository).selectTotalAnalyticByGroupId(anyLong());
        verify(cacheService).saveValue(anyString(), any(), any());
    }

    @Test
    void getAlreadyPaidByGroupId_GroupFoundAndCacheFound_CorrectReturnAndCheckRepo(){
        ResponseTotalAnalyticSubscriptionGroupDto responseTotalAnalyticSubscriptionGroupDto = new ResponseTotalAnalyticSubscriptionGroupDto(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000).multiply(BigDecimal.valueOf(12))
        );
        when(webClientService.checkUserInGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.just(responseTotalAnalyticSubscriptionGroupDto));

        Mono<ResponseTotalAnalyticSubscriptionGroupDto> result = analyticSubscriptionService.getAlreadyPaidByGroupId(anyLong(), any());

        StepVerifier.create(result)
                .expectNext(responseTotalAnalyticSubscriptionGroupDto)
                .verifyComplete();
        verify(webClientService).checkUserInGroup(anyLong(), any());
        verify(cacheService).getValue(any(), any());
        verify(analyticSubscriptionRepository, times(0)).selectTotalAnalyticByGroupId(anyLong());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
    }

    @Test
    @DisplayName("getAlreadyPaidByGroupId: found -> group, not found -> cache, repo -> null")
    void getAlreadyPaidByGroupId_GroupFoundAndResultIsNullAndCacheNotFound_CorrectReturnAndCheckRepo(){
        ResponseTotalAnalyticSubscriptionGroupDto responseTotalAnalyticSubscriptionGroupDto = new ResponseTotalAnalyticSubscriptionGroupDto(
                null,
                null,
                null
        );
        when(webClientService.checkUserInGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());
        when(analyticSubscriptionRepository.selectTotalAnalyticByGroupId(anyLong())).thenReturn(Mono.just(responseTotalAnalyticSubscriptionGroupDto));

        Mono<ResponseTotalAnalyticSubscriptionGroupDto> result = analyticSubscriptionService.getAlreadyPaidByGroupId(anyLong(), any());

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(webClientService).checkUserInGroup(anyLong(), any());
        verify(cacheService).getValue(any(), any());
        verify(analyticSubscriptionRepository).selectTotalAnalyticByGroupId(anyLong());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
    }

    @Test
    void getTotalAnalyticGroups_GroupsIdFound_CorrectReturn(){
        ResponseTotalAnalyticGroupsDto responseTotalAnalyticGroupsDto = new ResponseTotalAnalyticGroupsDto(5, BigDecimal.ONE);
        when(webClientService.getGroupsIdByOwnerId(any())).thenReturn(Mono.just(List.of(1L, 2L, 3L)));
        when(analyticSubscriptionRepository.selectTotalAnalyticByGroupsId(any())).thenReturn(Mono.just(responseTotalAnalyticGroupsDto));

        Mono<ResponseTotalAnalyticGroupsDto> result = analyticSubscriptionService.getTotalAnalyticGroups(any());

        StepVerifier.create(result)
                .expectNext(responseTotalAnalyticGroupsDto)
                .verifyComplete();
    }

    @Test
    void generateAnalyticSubscription_UserSubscriptionCorrect_CorrectSaveAndCheckRepo(){
        UserSubscription userSubscription = new UserSubscription();
        userSubscription.setAmount(BigDecimal.valueOf(1000));
        userSubscription.setStartDate(LocalDate.now().minusMonths(2L));
        userSubscription.setId(1L);
        userSubscription.setPaymentPeriod(PaymentPeriod.MONTHLY.toString());
        when(analyticSubscriptionRepository.insertAllAnalyticSubscription(any())).thenReturn(Mono.empty());

        Mono<Void> result = analyticSubscriptionService.generateAnalyticSubscription(userSubscription);
        StepVerifier.create(result)
                .verifyComplete();

        verify(analyticSubscriptionRepository).insertAllAnalyticSubscription(any());
    }

    @Test
    void generateAnalyticSubscription_UserSubscriptionCorrectAndSaveMoreSixty_CorrectSaveAndCheckRepo(){
        UserSubscription userSubscription = new UserSubscription();
        userSubscription.setAmount(BigDecimal.valueOf(1000));
        userSubscription.setStartDate(LocalDate.now().minusMonths(59L));
        userSubscription.setId(1L);
        userSubscription.setPaymentPeriod(PaymentPeriod.MONTHLY.toString());
        when(analyticSubscriptionRepository.insertAllAnalyticSubscription(any())).thenReturn(Mono.empty());

        Mono<Void> result = analyticSubscriptionService.generateAnalyticSubscription(userSubscription);
        StepVerifier.create(result)
                .verifyComplete();

        verify(analyticSubscriptionRepository, times(2)).insertAllAnalyticSubscription(any());
    }
}
