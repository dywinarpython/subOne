package com.subOne.subscriptions_service.dto.analytic_subscription;


import java.time.LocalDate;

public record SubscriptionLastDatePaymentDto(Long subscriptionId, String paymentPeriod, LocalDate datePaid) {
}
