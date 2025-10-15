package com.subOne.subscriptions_service.dto.subscription.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RequestUpdateSubscriptionDto(
        @Size(max = 20, message = "Service name must be at most 20 characters")
        String serviceName,

        @Size(max = 50, message = "Subscription name must be at most 50 characters")
        String subscriptionName,

        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        Boolean statusStop
        ) {
}
