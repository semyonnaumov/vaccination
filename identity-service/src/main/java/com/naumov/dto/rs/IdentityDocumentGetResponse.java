package com.naumov.dto.rs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naumov.dto.IdentifiableEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class IdentityDocumentGetResponse extends IdentifiableEntity {
    private String type;
    @JsonProperty("full_number")
    private String fullNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("issue_date")
    private LocalDate issueDate;
    @JsonProperty("is_primary")
    private Boolean isPrimary;

    @Builder
    public IdentityDocumentGetResponse(Long id, String type, String fullNumber, LocalDate issueDate, Boolean isPrimary) {
        super(id);
        this.type = type;
        this.fullNumber = fullNumber;
        this.issueDate = issueDate;
        this.isPrimary = isPrimary;
    }
}
