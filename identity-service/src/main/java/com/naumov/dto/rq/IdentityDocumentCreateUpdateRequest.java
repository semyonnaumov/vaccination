package com.naumov.dto.rq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.validation.annotation.NullableBoolean;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
public class IdentityDocumentCreateUpdateRequest {
    private Long id;
    @NotNull
    @Size(max = 30)
    private String type;
    @NotNull
    @Size(max = 20)
    @JsonProperty("full_number")
    private String fullNumber;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("issue_date")
    private LocalDate issueDate;
    @NullableBoolean
    @JsonProperty("is_primary")
    private Boolean isPrimary;

    public IdentityDocumentCreateUpdateRequest() {
        this.isPrimary = false;
    }
}

