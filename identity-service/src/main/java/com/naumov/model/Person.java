package com.naumov.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "people")
public class Person {
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
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL) // todo cascade?
    private List<PersonAddress> addressRecords;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL) // todo cascade?
    private List<Contact> contacts;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL) // todo cascade?
    private List<IdentityDocument> identityDocuments;
}
