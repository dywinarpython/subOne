package com.subOne.subscriptions_service.dto.subscription.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ResponseSubscriptionDto(Long id,
                                      String serviceName,
                                      String subscriptionName,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      String paymentPeriod,
                                      BigDecimal amount,
                                      String status,
                                      OffsetDateTime updatedAt) {
}
