package com.naumov.identityservice.dto.rs;

import com.naumov.identityservice.dto.IdentifiableEntity;
import lombok.Builder;

public class IdentityDocumentCreateUpdateResponse extends IdentifiableEntity {
    @Builder
    public IdentityDocumentCreateUpdateResponse(Long id) {
        super(id);
    }
}
