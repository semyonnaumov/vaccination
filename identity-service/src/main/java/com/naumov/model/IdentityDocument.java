package com.naumov.model;

import com.naumov.util.AbstractBuilder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

import static com.naumov.util.JsonUtil.*;

@Getter
@Setter
@Entity
@Table(
        name = "identity_documents",
        uniqueConstraints = @UniqueConstraint(name = "type_full_number_uk", columnNames = {"type", "full_number"})
)
public class IdentityDocument implements IdentifiableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "identity_documents_gen")
    @SequenceGenerator(name = "identity_documents_gen", sequenceName = "identity_documents_seq", allocationSize = 10)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private DocumentType type;
    @Column(name = "full_number", length = 20, nullable = false)
    private String fullNumber;
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Person owner;
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ",\"type\":\"" + type + "\"" +
                ",\"fullNumber\":\"" + translateEscapes(fullNumber) + "\"" +
                ",\"issueDate\":\"" + convertLocalDate(issueDate) + "\"" +
                ",\"ownerId\":" + extractId(owner) +
                ",\"isPrimary\":" + isPrimary +
                "}";
    }

    public enum DocumentType {
        INNER_PASSPORT,
        INTERNATIONAL_PASSPORT,
        PENSION_ID,
        MEDICAL_INSURANCE
    }

    // Manual builder since we want to preserve field defaults (Lombok's builder overwrites them)
    public static IdentityDocumentBuilder builder() {
        return new IdentityDocumentBuilder();
    }

    public static class IdentityDocumentBuilder extends AbstractBuilder<IdentityDocument> {
        private IdentityDocumentBuilder() {
            super(IdentityDocument::new);
        }

        public IdentityDocumentBuilder id(Long id) {
            getInstance().id = id;
            return this;
        }

        public IdentityDocumentBuilder type(DocumentType type) {
            getInstance().type = type;
            return this;
        }

        public IdentityDocumentBuilder fullNumber(String fullNumber) {
            getInstance().fullNumber = fullNumber;
            return this;
        }

        public IdentityDocumentBuilder issueDate(LocalDate issueDate) {
            getInstance().issueDate = issueDate;
            return this;
        }

        public IdentityDocumentBuilder owner(Person owner) {
            getInstance().owner = owner;
            return this;
        }

        public IdentityDocumentBuilder isPrimary(Boolean isPrimary) {
            getInstance().isPrimary = isPrimary;
            return this;
        }
    }
}
