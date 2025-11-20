package com.subOne.subscriptions_service.dto.subscription.request;

import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.validate.EndDateAfterNow;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
        @DecimalMax(value = "9999999999999.99", message = "Amount is too big")
        BigDecimal amount) {
}
