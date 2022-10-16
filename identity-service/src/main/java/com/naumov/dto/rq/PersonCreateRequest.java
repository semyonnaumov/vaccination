package com.naumov.dto.rq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.validation.annotation.NullableBoolean;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PersonCreateRequest {
    @NotNull
    @Size(max = 255)
    private String name;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
    @NullableBoolean
    @JsonProperty("is_hidden")
    private Boolean isHidden;
    @NotNull
    private List<AddressCreateRequest> addresses;
    @NotNull
    private List<ContactCreateRequest> contacts;
    @NotNull
    @JsonProperty("identity_documents")
    private List<IdentityDocumentCreateRequest> identityDocuments;

    public PersonCreateRequest() {
        this.isHidden = false;
        this.addresses = new ArrayList<>();
        this.contacts = new ArrayList<>();
        this.identityDocuments = new ArrayList<>();
    }
}

