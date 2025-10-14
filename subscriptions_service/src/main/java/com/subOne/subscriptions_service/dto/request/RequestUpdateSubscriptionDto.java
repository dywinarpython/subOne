package com.subOne.subscriptions_service.dto.request;

import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.validate.EndDateAfterNow;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RequestUpdateSubscriptionDto(
        @Size(max = 20, message = "Service name must be at most 20 characters")
        String serviceName,

        @Size(max = 50, message = "Subscription name must be at most 50 characters")
        String subscriptionName,

        LocalDate startDate,

        @EndDateAfterNow
        LocalDate endDate,

        PaymentPeriod paymentPeriod,

        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        Boolean statusStop
        ) {
}
