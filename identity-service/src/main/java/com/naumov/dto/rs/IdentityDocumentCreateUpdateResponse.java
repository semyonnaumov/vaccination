package com.naumov.dto.rs;

import com.naumov.dto.IdentifiableEntity;
import lombok.Builder;

public class IdentityDocumentCreateUpdateResponse extends IdentifiableEntity {
    @Builder
    public IdentityDocumentCreateUpdateResponse(Long id) {
        super(id);
    }
}
