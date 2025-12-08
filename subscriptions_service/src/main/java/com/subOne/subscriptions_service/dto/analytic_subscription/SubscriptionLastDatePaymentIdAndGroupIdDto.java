package com.subOne.subscriptions_service.dto.analytic_subscription;


import java.time.LocalDate;


public record SubscriptionLastDatePaymentIdAndGroupIdDto(Integer subscriptionId, Integer groupId, String paymentPeriod, LocalDate datePaid) {
}
