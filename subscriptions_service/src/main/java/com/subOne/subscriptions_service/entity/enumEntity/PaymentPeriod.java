package com.subOne.subscriptions_service.entity.enumEntity;

import java.time.Period;
import java.time.temporal.TemporalAmount;

public enum PaymentPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    public TemporalAmount generatePeriod(){
        return switch (this){
            case DAILY -> Period.ofDays(1);
            case WEEKLY -> Period.ofWeeks(1);
            case MONTHLY -> Period.ofMonths(1);
            case YEARLY -> Period.ofYears(1);
        };
    }
}
