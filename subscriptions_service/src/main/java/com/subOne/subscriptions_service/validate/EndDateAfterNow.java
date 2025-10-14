package com.subOne.subscriptions_service.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EndDateAfterNowValidator.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EndDateAfterNow {
    String message() default "EndDate must be in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
