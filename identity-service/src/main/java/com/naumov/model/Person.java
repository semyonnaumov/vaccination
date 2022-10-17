package com.naumov.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

import static com.naumov.util.JsonUtil.convertLocalDate;
import static com.naumov.util.JsonUtil.translateEscapes;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "people")
public class Person implements IdentifiableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "people_gen")
    @SequenceGenerator(name = "people_gen", sequenceName = "people_seq", allocationSize = 10)
    private Long id;
    @Column(name = "full_name", length = 150, nullable = false)
    private String name;
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden;
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PersonAddress> addressRecords;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Contact> contacts;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<IdentityDocument> identityDocuments;

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ",\"name\":\"" + translateEscapes(name) + "\"" +
                ",\"dateOfBirth\":\"" + convertLocalDate(dateOfBirth) + "\"" +
                ",\"isHidden\":" + isHidden +
                ",\"addressRecords\":" + addressRecords +
                ",\"contacts\":" + contacts +
                ",\"identityDocuments\":" + identityDocuments +
                "}";
    }
}
