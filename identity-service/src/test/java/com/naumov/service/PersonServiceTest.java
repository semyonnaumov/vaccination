package com.naumov.service;

import com.naumov.exception.PersonConsistencyException;
import com.naumov.model.*;
import com.naumov.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PersonServiceTest {
    @Autowired
    PersonService personService;
    @Autowired
    RegionRepository regionRepository;
    @Autowired
    PersonRepository personRepository;
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
        personRepository.flush();

        // assertions
        assertThat(savedPerson.getName()).isEqualTo(person.getName());
        assertThat(savedPerson.getDateOfBirth()).isEqualTo(person.getDateOfBirth());
        assertThat(savedPerson.getIsHidden()).isEqualTo(person.getIsHidden());
        assertThat(savedPerson.getAddressRecords().size()).isEqualTo(person.getAddressRecords().size());
        assertThat(savedPerson.getContacts().size()).isEqualTo(person.getContacts().size());
        assertThat(savedPerson.getIdentityDocuments().size()).isEqualTo(person.getIdentityDocuments().size());

        assertThat(personRepository.count()).isEqualTo(1);
        assertThat(addressRepository.count()).isEqualTo(2);
        assertThat(contactRepository.count()).isEqualTo(2);
        assertThat(identityDocumentRepository.count()).isEqualTo(3);

        Optional<Address> address0 = addressRepository.findByRegionNameAndAddress(region0.getName(), "Address line 0");
        assertThat(address0).isPresent();
        assertThat(address0.get().getPersonRecords().size()).isEqualTo(1);
        Optional<Address> address1 = addressRepository.findByRegionNameAndAddress(region1.getName(), "Address line 1");
        assertThat(address1).isPresent();
        assertThat(address1.get().getPersonRecords().size()).isEqualTo(1);

        assertThat(personAddressRepository.count()).isEqualTo(2);
        assertThat(contactRepository.count()).isEqualTo(2);
        assertThat(identityDocumentRepository.count()).isEqualTo(3);
    }

    @Test
    @Transactional
    void createPersonWithOneExistingAddress() {
        Region region0 = regionRepository.findAll().get(0);
        Region region1 = regionRepository.findAll().get(1);

        // prepare DB
        Person transientExistingPerson = Person.builder()
                .name("Person 0")
                .dateOfBirth(LocalDate.EPOCH)
                .isHidden(false)
                .build();
        addIdentityDocument(transientExistingPerson, IdentityDocument.DocumentType.INNER_PASSPORT, "12345", "1999-12-12", true);
        addAddressRecord(transientExistingPerson, region0, "Address line 0", true);
        Person existingPerson = personService.createPerson(transientExistingPerson);

        // test adding new person with existing address
        Person newPerson = Person.builder()
                .name("Person 1")
                .dateOfBirth(LocalDate.EPOCH)
                .isHidden(true)
                .build();
        addIdentityDocument(newPerson, IdentityDocument.DocumentType.INTERNATIONAL_PASSPORT, "0123401234", "2001-01-01", true);
        addAddressRecord(newPerson, region0, "Address line 0", false);
        addAddressRecord(newPerson, region1, "Address line 1", true);
        Person savedPerson = personService.createPerson(newPerson);

        personRepository.flush();

        // assertions
        assertThat(existingPerson.getAddressRecords().size()).isEqualTo(1);
        assertThat(savedPerson.getAddressRecords().size()).isEqualTo(2);
        assertThat(addressRepository.count()).isEqualTo(2);
        assertThat(personAddressRepository.count()).isEqualTo(3);
    }

    @Test
    @Transactional
    void createPersonWithoutName() {
        Person newPerson = prepareSimplePerson();
        newPerson.setName(null);
        assertThatThrownBy(() -> {
            personService.createPerson(newPerson);
            personRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void createPersonWithoutDateOfBirth() {
        Person newPerson = prepareSimplePerson();
        newPerson.setDateOfBirth(null);
        assertThatThrownBy(() -> {
            personService.createPerson(newPerson);
            personRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void createPersonWithoutIsHidden() {
        Person newPerson = prepareSimplePerson();
        newPerson.setIsHidden(null);
        assertThatThrownBy(() -> {
            personService.createPerson(newPerson);
            personRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void createPersonWithoutAddresses() {
        Person newPerson = prepareSimplePerson();
        newPerson.setAddressRecords(null);
        Person createdPerson = personService.createPerson(newPerson);
        personRepository.flush();
        assertThat(createdPerson.getAddressRecords()).isEmpty();
    }

    @Test
    @Transactional
    void createPersonWithoutContacts() {
        Person newPerson = prepareSimplePerson();
        newPerson.setContacts(null);
        Person createdPerson = personService.createPerson(newPerson);
        personRepository.flush();
        assertThat(createdPerson.getContacts()).isNull();
    }

    @Test
    @Transactional
    void createPersonWithoutIdentityDocuments() {
        Person newPerson = prepareSimplePerson();
        newPerson.setIdentityDocuments(null);
        assertThatThrownBy(() -> {
            personService.createPerson(newPerson);
            personRepository.flush();
        }).isInstanceOf(PersonConsistencyException.class);
    }

    private Person prepareSimplePerson() {
        Person person = Person.builder()
                .name("Name")
                .dateOfBirth(LocalDate.EPOCH)
                .isHidden(true)
                .build();

        prepareSimpleContact(person);
        prepareSimpleIdentityDocument(person);

        Region region0 = regionRepository.findAll().get(0);
        addAddressRecord(person, region0, "Address", true);
        return person;
    }

    private void prepareSimpleIdentityDocument(Person person) {
        addIdentityDocument(
                person,
                IdentityDocument.DocumentType.INNER_PASSPORT,
                "12345",
                "1999-12-12",
                true
        );
    }

    private void prepareSimpleContact(Person person) {
        addContact(person, "+71234567890");
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