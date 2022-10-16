package com.naumov.dto.rq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.IdentifiableEntity;
import com.naumov.dto.validation.annotation.NullableBoolean;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
public class IdentityDocumentUpdateRequest extends IdentifiableEntity {
    @Size(max = 30)
    private String type;
    @Size(max = 20)
    @JsonProperty("full_number")
    private String fullNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("issue_date")
    private LocalDate issueDate;
    @NullableBoolean
    @JsonProperty("is_primary")
    private Boolean isPrimary;
}
