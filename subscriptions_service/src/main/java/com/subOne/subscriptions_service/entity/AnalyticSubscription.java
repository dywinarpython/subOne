package com.subOne.subscriptions_service.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table(name = "analytic_subscriptions")
@Getter
@Setter
public class AnalyticSubscription {
    @Id
    private Long id;
    private Long subscriptionId;
    private LocalDate datePaid;
}
