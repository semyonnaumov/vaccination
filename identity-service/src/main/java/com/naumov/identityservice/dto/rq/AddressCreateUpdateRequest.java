package com.naumov.identityservice.dto.rq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.identityservice.dto.validation.annotation.NullableBoolean;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class AddressCreateUpdateRequest {
    private Long id;
    @NotNull
    @Size(max = 20)
    private String region;
    @NotNull
    @Size(max = 255)
    private String address;
    @NullableBoolean
    @JsonProperty("registration_address")
    private Boolean isRegistrationAddress;

    public AddressCreateUpdateRequest() {
        this.isRegistrationAddress = false;
    }
}