package com.naumov.model;

import com.naumov.util.AbstractBuilder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.naumov.util.JsonUtil.convertLocalDate;
import static com.naumov.util.JsonUtil.translateEscapes;

@Entity
@Table(name = "people")
public class Person implements IdentifiableEntity {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "people_gen")
    @SequenceGenerator(name = "people_gen", sequenceName = "people_seq", allocationSize = 10)
    private Long id;
    @Getter
    @Setter
    @Column(name = "full_name", length = 150, nullable = false)
    private String name;
    @Getter
    @Setter
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    @Getter
    @Setter
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<PersonAddress> addressRecords = new ArrayList<>();
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IdentityDocument> identityDocuments = new ArrayList<>();

    public List<PersonAddress> getAddressRecords() {
        return addressRecords;
    }

    public void setAddressRecords(List<PersonAddress> addressRecords) {
        this.addressRecords = Optional.ofNullable(addressRecords).orElseGet(ArrayList::new);
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = Optional.ofNullable(contacts).orElseGet(ArrayList::new);
    }

    public List<IdentityDocument> getIdentityDocuments() {
        return identityDocuments;
    }

    public void setIdentityDocuments(List<IdentityDocument> identityDocuments) {
        this.identityDocuments = Optional.ofNullable(identityDocuments).orElseGet(ArrayList::new);
    }

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

    // Manual builder since we want to preserve field defaults (Lombok's builder overwrites them)
    public static PersonBuilder builder() {
        return new PersonBuilder();
    }

    public static class PersonBuilder extends AbstractBuilder<Person> {
        private PersonBuilder() {
            super(Person::new);
        }

        public PersonBuilder id(Long id) {
            getInstance().id = id;
            return this;
        }

        public PersonBuilder name(String name) {
            getInstance().name = name;
            return this;
        }

        public PersonBuilder dateOfBirth(LocalDate dateOfBirth) {
            getInstance().dateOfBirth = dateOfBirth;
            return this;
        }

        public PersonBuilder isHidden(Boolean isHidden) {
            getInstance().isHidden = isHidden;
            return this;
        }

        public PersonBuilder addressRecords(List<PersonAddress> addressRecords) {
            getInstance().addressRecords = Optional.ofNullable(addressRecords).orElseGet(ArrayList::new);
            return this;
        }

        public PersonBuilder contacts(List<Contact> contacts) {
            getInstance().contacts = Optional.ofNullable(contacts).orElseGet(ArrayList::new);
            return this;
        }

        public PersonBuilder identityDocuments(List<IdentityDocument> identityDocuments) {
            getInstance().identityDocuments = Optional.ofNullable(identityDocuments).orElseGet(ArrayList::new);
            return this;
        }
    }
}
