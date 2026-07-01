package edu.rutmiit.demo.cinemaapicontract.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "Некорректный номер телефона";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
