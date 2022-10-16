package com.naumov.dto.rq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.validation.annotation.NullablePhoneNumber;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ContactCreateRequest {
    @NotNull
    @NullablePhoneNumber
    @JsonProperty("phone_number")
    private String phoneNumber;
}