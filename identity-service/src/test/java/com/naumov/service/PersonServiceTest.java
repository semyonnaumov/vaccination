package com.naumov.service;

import com.naumov.model.*;
import com.naumov.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class PersonServiceTest {
    @Autowired
    PersonService personService;
    @Autowired
    RegionRepository regionRepository;

    @Test
    @Transactional
    void createPersonWithAllNewRelations() {
        Person person = Person.builder()
                .name("Person")
                .dateOfBirth(LocalDate.EPOCH)
                .isHidden(true)
                .build();

        Region region0 = regionRepository.findAll().get(0);
        Region region1 = regionRepository.findAll().get(1);
        addAddressRecord(person, region0, "Address line 0", true);
        addAddressRecord(person, region1, "Address line 1", false);

        addContact(person, "+71234567890");
        addContact(person, "+71234567891");

        addIdentityDocument(person, IdentityDocument.DocumentType.INNER_PASSPORT, "12345", "1999-12-12", true);
        addIdentityDocument(person, IdentityDocument.DocumentType.INTERNATIONAL_PASSPORT, "0123401234", "2001-01-01", false);
        addIdentityDocument(person, IdentityDocument.DocumentType.PENSION_ID, "2626262626262626", "2021-12-21", false);

        personService.createPerson(person);

        // asserts
    }

    private void addContact(Person person, String phoneNumber) {
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

    private void addAddressRecord(Person person, Region region, String addressString, Boolean isRegistration) {
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

    private void addIdentityDocument(Person person,
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
}