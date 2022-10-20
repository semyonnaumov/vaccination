package com.naumov;

import com.naumov.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public final class EntityTestUtil {

    public static SimplePersonBuilder simplePersonBuilder(Region addressRegion) {
        Objects.requireNonNull(addressRegion, "Address region must not be null");
        return new SimplePersonBuilder(addressRegion);
    }

    public static final class SimplePersonBuilder {
        private final Region addressRegion;
        private String name = "Name";
        private LocalDate dateOfBirth = LocalDate.EPOCH;
        private Boolean isHidden = true;
        private String phoneNumber = "+71234567890";
        private IdentityDocument.DocumentType documentType = IdentityDocument.DocumentType.INNER_PASSPORT;
        private String documentFullNumber = "12345";
        private String documentIssueDate = "1999-12-12";
        private Boolean documentIsPrimary = true;
        private String addressString = "Address";
        private Boolean addressIsRegistration = true;

        public SimplePersonBuilder(Region addressRegion) {
            this.addressRegion = addressRegion;
        }

        public SimplePersonBuilder name(String name) {
            this.name = name;
            return this;
        }

        public SimplePersonBuilder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public SimplePersonBuilder isHidden(Boolean isHidden) {
            this.isHidden = isHidden;
            return this;
        }

        public SimplePersonBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public SimplePersonBuilder documentType(IdentityDocument.DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        public SimplePersonBuilder documentFullNumber(String documentFullNumber) {
            this.documentFullNumber = documentFullNumber;
            return this;
        }

        public SimplePersonBuilder documentIssueDate(String documentIssueDate) {
            this.documentIssueDate = documentIssueDate;
            return this;
        }

        public SimplePersonBuilder documentIsPrimary(Boolean documentIsPrimary) {
            this.documentIsPrimary = documentIsPrimary;
            return this;
        }

        public SimplePersonBuilder addressString(String addressString) {
            this.addressString = addressString;
            return this;
        }

        public SimplePersonBuilder addressIsRegistration(Boolean addressIsRegistration) {
            this.addressIsRegistration = addressIsRegistration;
            return this;
        }

        public Person build() {
            Person person = Person.builder()
                    .name(name)
                    .dateOfBirth(dateOfBirth)
                    .isHidden(isHidden)
                    .build();

            addContact(person, phoneNumber);
            addIdentityDocument(
                    person,
                    documentType,
                    documentFullNumber,
                    documentIssueDate,
                    documentIsPrimary
            );

            Objects.requireNonNull(addressRegion, "Address region is null");
            addAddressRecord(
                    person,
                    addressRegion,
                    addressString,
                    addressIsRegistration
            );

            return person;
        }
    }

    public static void addContact(Person person, String phoneNumber) {
        Contact contact = Contact.builder()
                .phoneNumber(phoneNumber)
                .owner(person)
                .build();

        List<Contact> contacts = person.getContacts() != null
                ? person.getContacts()
                : new ArrayList<>();

        contacts.add(contact);
        person.setContacts(contacts);
    }

    public static void addAddressRecord(Person person, Region region, String addressString, Boolean isRegistration) {
        Address address = Address.builder()
                .region(region)
                .address(addressString)
                .build();

        PersonAddress personAddress = PersonAddress.builder()
                .person(person)
                .address(address)
                .isRegistration(isRegistration)
                .build();

        address.setPersonRecords(List.of(personAddress));

        List<PersonAddress> personAddresses = person.getAddressRecords() != null
                ? person.getAddressRecords()
                : new ArrayList<>();

        personAddresses.add(personAddress);
        person.setAddressRecords(personAddresses);
    }

    public static void addIdentityDocument(Person person,
                                           IdentityDocument.DocumentType type,
                                           String fullNumber,
                                           String issueDate,
                                           boolean isPrimary) {
        IdentityDocument identityDocument = IdentityDocument.builder()
                .type(type)
                .fullNumber(fullNumber)
                .issueDate(LocalDate.parse(issueDate, DateTimeFormatter.ISO_LOCAL_DATE))
                .owner(person)
                .isPrimary(isPrimary)
                .build();

        List<IdentityDocument> identityDocuments = person.getIdentityDocuments() != null
                ? person.getIdentityDocuments()
                : new ArrayList<>();

        identityDocuments.add(identityDocument);
        person.setIdentityDocuments(identityDocuments);
    }

    public static void assertThatCollectionsAreNullOrEqualSize(Collection<?> actual, Collection<?> expected) {
        if (expected == null) {
            assertThat(actual).isNull();
        } else {
            assertThat(actual).isNotNull();
            assertThat(actual.size()).isEqualTo(expected.size());
        }
    }
}