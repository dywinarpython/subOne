package com.subOne.subscriptions_service.dto.analytic_subscription;


import java.math.BigDecimal;
import java.time.LocalDate;


public record SubscriptionLastDatePaymentDto(Integer subscriptionId, String paymentPeriod, BigDecimal amount, LocalDate datePaid) {
}
