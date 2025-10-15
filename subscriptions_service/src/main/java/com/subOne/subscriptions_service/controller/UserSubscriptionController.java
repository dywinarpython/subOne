package com.subOne.subscriptions_service.controller;

import com.subOne.subscriptions_service.dto.subscription.request.RequestSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.request.RequestUpdateSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionsDto;
import com.subOne.subscriptions_service.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Tag(name = "Управление подписками")
@RequestMapping("/api/v1/groups/{groupId}/subscriptions")
@RestController
@RequiredArgsConstructor
public class UserSubscriptionController {
    private final UserSubscriptionService userSubscriptionService;

    @Operation(
            summary = "Получение всех подписок для определенной группы",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = ResponseSubscriptionsDto.class)
                    )
            )
    )
    @GetMapping
    public Mono<ResponseSubscriptionsDto> getSubscriptionsGroup(@PathVariable Long groupId, @AuthenticationPrincipal Jwt jwt){
        return userSubscriptionService.getSubscriptionsGroup(groupId, jwt);
    }

    @Operation(
            summary = "Получение подписки по id для определенной группы",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = ResponseSubscriptionsDto.class)
                    )
            )
    )
    @GetMapping("/{subscriptionId}")
    public Mono<ResponseSubscriptionDto> getSubscriptionById(@PathVariable Long groupId, @PathVariable Long subscriptionId, @AuthenticationPrincipal Jwt jwt){
        return userSubscriptionService.getSubscriptionById(groupId, subscriptionId, jwt);
    }


    @Operation(
            summary = "Создание подписки для определенной группы",
            responses = @ApiResponse(
                    responseCode = "201",
                    content = @Content(
                            schema = @Schema(implementation = ResponseSubscriptionDto.class)
                    )
            )
    )
    @PostMapping
    public Mono<ResponseSubscriptionDto> saveSubscription(@PathVariable Long groupId,
                                                          @Valid @RequestBody Mono<RequestSubscriptionDto> requestSubscriptionDtoMono,
                                                          @AuthenticationPrincipal Jwt jwt){
        return userSubscriptionService.saveSubscription(groupId, requestSubscriptionDtoMono, jwt);
    }

    @Operation(
            summary = "Изменение подписки для определенной группы",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = Map.class)
                    )
            )
    )
    @PatchMapping("/{subscriptionId}")
    public Mono<ResponseEntity<Map<String, String>>> updateSubscription(@PathVariable Long groupId,
                                                        @PathVariable Long subscriptionId,
                                                        @Valid @RequestBody Mono<RequestUpdateSubscriptionDto> requestSubscriptionDtoMono,
                                                        @AuthenticationPrincipal Jwt jwt){
        return userSubscriptionService.updateSubscription(groupId, subscriptionId, requestSubscriptionDtoMono, jwt)
                .thenReturn(ResponseEntity.ok(Map.of("message", "Subscription is update")));
    }

    @Operation(
            summary = "Продления подписки по числу (число = значение из PaymentPeriod)",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = Map.class)
                    )
            )
    )
    @PatchMapping("/{subscriptionId}/extend/{extensionCount}")
    public Mono<ResponseEntity<Map<String, String>>> extendSubscription(@PathVariable Long groupId,
                                                                        @PathVariable Long subscriptionId,
                                                                        @PathVariable Long extensionCount,
                                                                        @AuthenticationPrincipal Jwt jwt){
        return userSubscriptionService.renewSubscriptionById(groupId, subscriptionId, extensionCount, jwt)
                .thenReturn(ResponseEntity.ok(Map.of("message", "Subscription extended")));
    }




    @Operation(
            summary = "Удаление подписки для определенной группы",
            responses = @ApiResponse(
                    responseCode = "204"
            )
    )
    @DeleteMapping("/{subscriptionId}")
    public Mono<ResponseEntity<Void>> deleteSubscription(@PathVariable Long groupId,
                                                         @PathVariable Long subscriptionId,
                                                         @AuthenticationPrincipal Jwt jwt){
        return userSubscriptionService.deleteSubscriptionById(groupId, subscriptionId, jwt).thenReturn(ResponseEntity.noContent().build());
    }
}
