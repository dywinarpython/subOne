package com.subOne.subscriptions_service.dto.analytic_subscription.response;

import java.math.BigDecimal;

public record ResponseTotalAnalyticGroupsDto(Integer count, BigDecimal totalSum) {
}
