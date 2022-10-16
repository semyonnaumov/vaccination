package com.naumov.dto.rs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.IdentifiableEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
public class PersonGetBulkResponse extends IdentifiableEntity {
    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
    @JsonProperty("main_identity_document")
    private IdentityDocumentGetResponse mainIdentityDocument;
    private ContactGetResponse contact;
    @JsonProperty("registration_address")
    private AddressGetResponse registrationAddress;

    @Builder
    public PersonGetBulkResponse(@NotNull Long id,
                                 String name,
                                 LocalDate dateOfBirth,
                                 IdentityDocumentGetResponse mainIdentityDocument,
                                 ContactGetResponse contact,
                                 AddressGetResponse registrationAddress) {
        super(id);
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.mainIdentityDocument = mainIdentityDocument;
        this.contact = contact;
        this.registrationAddress = registrationAddress;
    }
}
