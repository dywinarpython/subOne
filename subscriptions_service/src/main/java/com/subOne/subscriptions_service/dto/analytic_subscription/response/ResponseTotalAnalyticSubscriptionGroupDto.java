package com.subOne.subscriptions_service.dto.analytic_subscription.response;

import java.math.BigDecimal;

public record ResponseTotalAnalyticSubscriptionGroupDto(BigDecimal totalAmount, BigDecimal approxMonthPaid, BigDecimal approxYearPaid) {
}
