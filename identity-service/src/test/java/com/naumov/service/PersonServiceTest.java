package com.naumov.service;

import com.naumov.model.*;
import com.naumov.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PersonServiceTest {
    @Autowired
    PersonService personService;
    @Autowired
    PersonRepository personRepository;
    @Autowired
    RegionRepository regionRepository;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    ContactRepository contactRepository;
    @Autowired
    IdentityDocumentRepository identityDocumentRepository;
    @Autowired
    PersonAddressRepository personAddressRepository;

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

        Person savedPerson = personService.createPerson(person);
        assertThat(savedPerson.getName()).isEqualTo(person.getName());
        assertThat(savedPerson.getDateOfBirth()).isEqualTo(person.getDateOfBirth());
        assertThat(savedPerson.getIsHidden()).isEqualTo(person.getIsHidden());
        assertThat(savedPerson.getAddressRecords().size()).isEqualTo(person.getAddressRecords().size());
        assertThat(savedPerson.getContacts().size()).isEqualTo(person.getContacts().size());
        assertThat(savedPerson.getIdentityDocuments().size()).isEqualTo(person.getIdentityDocuments().size());

        // assertions
        assertThat(personRepository.findAll().size()).isEqualTo(1);
        assertThat(addressRepository.findAll().size()).isEqualTo(2);
        assertThat(contactRepository.findAll().size()).isEqualTo(2);
        assertThat(identityDocumentRepository.findAll().size()).isEqualTo(3);

        Optional<Address> address0 = addressRepository.findByRegionNameAndAddress(region0.getName(), "Address line 0");
        assertThat(address0).isPresent();
        assertThat(address0.get().getPersonRecords().size()).isEqualTo(1);
        Optional<Address> address1 = addressRepository.findByRegionNameAndAddress(region1.getName(), "Address line 1");
        assertThat(address1).isPresent();
        assertThat(address1.get().getPersonRecords().size()).isEqualTo(1);

        assertThat(personAddressRepository.findAll().size()).isEqualTo(2);
        assertThat(contactRepository.findAll().size()).isEqualTo(2);
        assertThat(identityDocumentRepository.findAll().size()).isEqualTo(3);

        System.out.println(person);
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