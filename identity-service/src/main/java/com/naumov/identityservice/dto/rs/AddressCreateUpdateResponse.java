package com.naumov.identityservice.dto.rs;

import com.naumov.identityservice.dto.IdentifiableEntity;
import lombok.Builder;

public class AddressCreateUpdateResponse extends IdentifiableEntity {
    @Builder
    public AddressCreateUpdateResponse(Long id) {
        super(id);
    }
}
