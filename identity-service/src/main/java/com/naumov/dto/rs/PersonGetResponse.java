package com.naumov.dto.rs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.IdentifiableEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PersonGetResponse extends IdentifiableEntity {
    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
    @JsonProperty("is_hidden")
    private Boolean isHidden;
    private List<AddressGetResponse> addresses;
    private List<ContactGetResponse> contacts;
    @JsonProperty("identity_documents")
    private List<IdentityDocumentGetResponse> identityDocuments;

    @Builder
    public PersonGetResponse(@NotNull Long id,
                             String name,
                             LocalDate dateOfBirth,
                             Boolean isHidden,
                             List<AddressGetResponse> addresses,
                             List<ContactGetResponse> contacts,
                             List<IdentityDocumentGetResponse> identityDocuments) {
        super(id);
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.isHidden = isHidden;
        this.addresses = addresses;
        this.contacts = contacts;
        this.identityDocuments = identityDocuments;
    }
}
