package com.jumbo.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidTimeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTime {
    String message() default "Invalid time format, expected HH:mm";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}