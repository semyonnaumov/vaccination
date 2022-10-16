package com.naumov.dto.rs;

import com.naumov.dto.IdentifiableEntity;
import lombok.Builder;

public class AddressCreateUpdateResponse extends IdentifiableEntity {
    @Builder
    public AddressCreateUpdateResponse(Long id) {
        super(id);
    }
}
