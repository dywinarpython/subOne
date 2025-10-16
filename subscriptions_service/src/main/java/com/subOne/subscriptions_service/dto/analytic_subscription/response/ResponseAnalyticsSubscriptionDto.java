package com.subOne.subscriptions_service.dto.analytic_subscription.response;

import java.util.List;

public record ResponseAnalyticsSubscriptionDto(ResponseTotalAnalyticSubscriptionDto totalAnalytic,
                                               List<ResponseAnalyticPaymentSubscriptionDto> analyticPayments) {

}
