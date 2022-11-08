package com.naumov.identityservice.dto.rs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.identityservice.dto.IdentifiableEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactGetResponse extends IdentifiableEntity {
    @JsonProperty("phone_number")
    private String phoneNumber;

    @Builder
    public ContactGetResponse(Long id, String phoneNumber) {
        super(id);
        this.phoneNumber = phoneNumber;
    }
}