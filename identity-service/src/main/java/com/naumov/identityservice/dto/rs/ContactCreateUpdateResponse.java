package com.naumov.identityservice.dto.rs;

import com.naumov.identityservice.dto.IdentifiableEntity;
import lombok.Builder;

public class ContactCreateUpdateResponse extends IdentifiableEntity {
    @Builder
    public ContactCreateUpdateResponse(Long id) {
        super(id);
    }
}
