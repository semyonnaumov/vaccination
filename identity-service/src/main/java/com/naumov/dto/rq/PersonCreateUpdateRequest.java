package com.naumov.dto.rq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.validation.annotation.NullableBoolean;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PersonCreateUpdateRequest {
    private Long id;
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
    private List<AddressCreateUpdateRequest> addresses;
    @NotNull
    private List<ContactCreateUpdateRequest> contacts;
    @NotNull
    @JsonProperty("identity_documents")
    @NotEmpty
    private List<IdentityDocumentCreateUpdateRequest> identityDocuments;

    public PersonCreateUpdateRequest() {
        this.isHidden = false;
        this.addresses = new ArrayList<>();
        this.contacts = new ArrayList<>();
        this.identityDocuments = new ArrayList<>();
    }
}

