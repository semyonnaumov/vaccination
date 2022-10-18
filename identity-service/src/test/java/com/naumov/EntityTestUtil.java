package com.naumov;

import com.naumov.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public final class EntityTestUtil {
    public static Person prepareSimplePerson(Region managedRegion) {
        Person person = Person.builder()
                .name("Name")
                .dateOfBirth(LocalDate.EPOCH)
                .isHidden(true)
                .build();

        addContact(person, "+71234567890");
        addIdentityDocument(
                person,
                IdentityDocument.DocumentType.INNER_PASSPORT,
                "12345",
                "1999-12-12",
                true
        );

        addAddressRecord(person, managedRegion, "Address", true);
        return person;
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
