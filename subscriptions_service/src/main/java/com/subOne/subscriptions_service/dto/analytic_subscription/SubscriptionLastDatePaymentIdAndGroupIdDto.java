package com.subOne.subscriptions_service.dto.analytic_subscription;


import java.time.LocalDate;


public record SubscriptionLastDatePaymentIdAndGroupIdDto(Long subscriptionId, Long groupId, String paymentPeriod, LocalDate datePaid) {
}
