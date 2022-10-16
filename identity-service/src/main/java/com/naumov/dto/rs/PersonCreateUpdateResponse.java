package com.naumov.dto.rs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.IdentifiableEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class PersonCreateUpdateResponse extends IdentifiableEntity {
    @JsonProperty("is_hidden")
    private Boolean isHidden;
    private List<AddressCreateUpdateResponse> addresses;
    private List<ContactCreateUpdateResponse> contacts;
    @JsonProperty("identity_documents")
    private List<IdentityDocumentCreateUpdateResponse> identityDocuments;

    @Builder
    public PersonCreateUpdateResponse(@NotNull Long id,
                                      Boolean isHidden,
                                      List<AddressCreateUpdateResponse> addresses,
                                      List<ContactCreateUpdateResponse> contacts,
                                      List<IdentityDocumentCreateUpdateResponse> identityDocuments) {
        super(id);
        this.isHidden = isHidden;
        this.addresses = addresses;
        this.contacts = contacts;
        this.identityDocuments = identityDocuments;
    }
}
