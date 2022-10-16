package com.naumov.dto.rs;

import com.naumov.dto.IdentifiableEntity;
import lombok.Builder;

public class ContactCreateUpdateResponse extends IdentifiableEntity {
    @Builder
    public ContactCreateUpdateResponse(Long id) {
        super(id);
    }
}
