package com.naumov.dto.validation.annotation;

import javax.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Pattern(regexp = "\\+7[0-9]{10}", message = "allowed input: valid phone number (regexp = \"\\+7[0-9]{10}\")")
public @interface NullablePhoneNumber {
}
