package com.naumov.identityservice.dto.validation.annotation;

import javax.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Pattern(regexp = "^true$|^false$", message = "allowed input: true or false")
public @interface NullableBoolean {
}
