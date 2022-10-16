package com.naumov.dto.rq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.IdentifiableEntity;
import com.naumov.dto.validation.annotation.NullableBoolean;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
public class AddressUpdateRequest extends IdentifiableEntity {
    @Size(max = 20)
    private String region;
    @Size(max = 255)
    private String address;
    @NullableBoolean
    @JsonProperty("registration_address")
    private Boolean isRegistrationAddress;
}
