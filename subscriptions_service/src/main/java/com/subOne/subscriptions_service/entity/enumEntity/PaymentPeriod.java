package com.subOne.subscriptions_service.entity.enumEntity;

import java.time.Period;
import java.time.temporal.TemporalAmount;

public enum PaymentPeriod {
    MONTHLY,
    YEARLY;

    public TemporalAmount generatePeriod(){
        return switch (this){
            case MONTHLY -> Period.ofMonths(1);
            case YEARLY -> Period.ofYears(1);
        };
    }
}
