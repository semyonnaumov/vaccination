package com.naumov.dto.rs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.IdentifiableEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressGetResponse extends IdentifiableEntity {
    private String region;
    private String address;
    @JsonProperty("registration_address")
    private Boolean isRegistrationAddress;

    @Builder
    public AddressGetResponse(Long id, String region, String address, Boolean isRegistrationAddress) {
        super(id);
        this.region = region;
        this.address = address;
        this.isRegistrationAddress = isRegistrationAddress;
    }
}
