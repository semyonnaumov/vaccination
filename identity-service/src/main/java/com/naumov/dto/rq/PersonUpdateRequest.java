package com.naumov.dto.rq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.IdentifiableEntity;
import com.naumov.dto.validation.annotation.NullableBoolean;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PersonUpdateRequest extends IdentifiableEntity {
    @Size(max = 255)
    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
    @NullableBoolean
    @JsonProperty("is_hidden")
    private Boolean isHidden;
    private List<AddressUpdateRequest> addresses;
    private List<ContactUpdateRequest> contacts;
    @JsonProperty("identity_documents")
    private List<IdentityDocumentUpdateRequest> identityDocuments;
}


