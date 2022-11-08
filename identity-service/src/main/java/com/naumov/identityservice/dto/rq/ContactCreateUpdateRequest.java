package com.naumov.identityservice.dto.rq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.identityservice.dto.validation.annotation.NotNullPhoneNumber;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactCreateUpdateRequest {
    private Long id;
    @NotNullPhoneNumber
    @JsonProperty("phone_number")
    private String phoneNumber;
}