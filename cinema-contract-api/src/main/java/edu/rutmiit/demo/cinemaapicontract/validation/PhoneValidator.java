package edu.rutmiit.demo.cinemaapicontract.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{10,14}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String cleaned = value.replaceAll("[\\s()-]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }
}
