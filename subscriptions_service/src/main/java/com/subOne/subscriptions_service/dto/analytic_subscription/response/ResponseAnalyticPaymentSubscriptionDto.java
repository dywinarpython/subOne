package com.subOne.subscriptions_service.dto.analytic_subscription.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseAnalyticPaymentSubscriptionDto(LocalDate datePaid, BigDecimal amount) {
}
