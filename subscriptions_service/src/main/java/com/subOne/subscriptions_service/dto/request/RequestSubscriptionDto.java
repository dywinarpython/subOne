package com.subOne.subscriptions_service.dto.request;

import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.validate.EndDateAfterNow;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public record RequestSubscriptionDto(
        @NotNull(message = "Service name is not null")
        @Size(max = 20, message = "Service name must be at most 20 characters")
        String serviceName,

        @NotNull(message = "SubscriptionName is not null")
        @Size(max = 50, message = "Subscription name must be at most 50 characters")
        String subscriptionName,

        @NotNull(message = "StartDate is not null")
        LocalDate startDate,

        @NotNull(message = "EndDate is not null")
        @EndDateAfterNow
        LocalDate endDate,

        @NotNull(message = "PaymentPeriod is not null")
        PaymentPeriod paymentPeriod,

        @NotNull(message = "Amount is not null")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount) {
}
