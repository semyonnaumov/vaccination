package com.naumov.dto.rq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.validation.annotation.NullableBoolean;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class AddressCreateRequest {
    @NotNull
    @Size(max = 20)
    private String region;
    @NotNull
    @Size(max = 255)
    private String address;
    @NullableBoolean
    @JsonProperty("registration_address")
    private Boolean isRegistrationAddress;

    public AddressCreateRequest() {
        this.isRegistrationAddress = false;
    }
}