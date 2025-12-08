package com.subOne.subscriptions_service.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Table("user_subscriptions")
@Getter
@Setter
public class UserSubscription {
    @Id
    private Integer id;
    private Integer groupId;
    private String serviceName;
    private String subscriptionName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String paymentPeriod;
    private BigDecimal amount;
    private String status;
    @CreatedDate
    private LocalDateTime createdAt;
    private OffsetDateTime updatedAt;
}
