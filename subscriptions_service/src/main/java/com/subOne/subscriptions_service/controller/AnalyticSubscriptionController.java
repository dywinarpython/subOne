package com.subOne.subscriptions_service.controller;

import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticPaymentSubscriptionsDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionGroupDto;
import com.subOne.subscriptions_service.service.AnalyticSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "Управление аналитикой подписок")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Управление аналитикой подписок")
public class AnalyticSubscriptionController {

    private final AnalyticSubscriptionService analyticSubscriptionService;

    @Operation(
            summary = "Получение аналитических данных подписки",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = ResponseTotalAnalyticSubscriptionDto.class)
                    )
            )
    )
    @GetMapping("/{subscriptionId}/analytic")
    public Mono<ResponseTotalAnalyticSubscriptionDto> getAnalyticSubscriptionById(@PathVariable Long groupId,
                                                                          @PathVariable Long subscriptionId,
                                                                          @AuthenticationPrincipal Jwt jwt){
        return analyticSubscriptionService.getTotalAnalyticById(groupId, subscriptionId, jwt);
    }

    @Operation(
            summary = "Получение данных оплаты",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = ResponseAnalyticPaymentSubscriptionsDto.class)
                    )
            )
    )
    @GetMapping("/{subscriptionId}/payment-info")
    public Mono<ResponseAnalyticPaymentSubscriptionsDto> getPaymentInfoSubscriptionById(@PathVariable Long groupId,
                                                                                        @PathVariable Long subscriptionId,
                                                                                        @RequestParam Integer page,
                                                                                        @AuthenticationPrincipal Jwt jwt){
        return analyticSubscriptionService.getPaymentInfoSubscriptionById(groupId, subscriptionId, page, jwt);
    }

    @Operation(
            summary = "Получение аналитических данных группы (контрольная сумма, примерная -> оплата в месяц, оплата в год)",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = ResponseTotalAnalyticSubscriptionDto.class)
                    )
            )
    )
    @GetMapping("/analytic/group-summary")
    public Mono<ResponseTotalAnalyticSubscriptionGroupDto> getAlreadyPaidGroup(@PathVariable Long groupId,
                                                                               @AuthenticationPrincipal Jwt jwt){
        return analyticSubscriptionService.getAlreadyPaidByGroupId(groupId, jwt);
    }
}

