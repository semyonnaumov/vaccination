package com.naumov.dto.rq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.IdentifiableEntity;
import com.naumov.dto.validation.annotation.NullablePhoneNumber;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactUpdateRequest extends IdentifiableEntity {
    @NullablePhoneNumber
    @JsonProperty("phone_number")
    private String phoneNumber;
}
