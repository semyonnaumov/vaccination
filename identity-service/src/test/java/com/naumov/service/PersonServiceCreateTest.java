package com.naumov.service;

import com.naumov.EntityTestUtil;
import com.naumov.exception.PersonCreationException;
import com.naumov.model.Address;
import com.naumov.model.IdentityDocument;
import com.naumov.model.Person;
import com.naumov.model.Region;
import com.naumov.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static com.naumov.EntityTestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PersonServiceCreateTest {
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
        Person newPerson = Person.builder()
                .name("Person")
                .dateOfBirth(LocalDate.EPOCH)
                .isHidden(true)
                .build();

        Region region0 = regionRepository.findAll().get(0);
        Region region1 = regionRepository.findAll().get(1);
        addAddressRecord(newPerson, region0, "Address line 0", true);
        addAddressRecord(newPerson, region1, "Address line 1", false);

        addContact(newPerson, "+71234567890");
        addContact(newPerson, "+71234567891");

        addIdentityDocument(newPerson, IdentityDocument.DocumentType.INNER_PASSPORT, "12345", "1999-12-12", true);
        addIdentityDocument(newPerson, IdentityDocument.DocumentType.INTERNATIONAL_PASSPORT, "0123401234", "2001-01-01", false);
        addIdentityDocument(newPerson, IdentityDocument.DocumentType.PENSION_ID, "2626262626262626", "2021-12-21", false);

        Person savedPerson = personService.createPerson(newPerson);
        personRepository.flush();

        // assertions
        assertThat(savedPerson.getName()).isEqualTo(newPerson.getName());
        assertThat(savedPerson.getDateOfBirth()).isEqualTo(newPerson.getDateOfBirth());
        assertThat(savedPerson.getIsHidden()).isEqualTo(newPerson.getIsHidden());
        assertThat(savedPerson.getAddressRecords().size()).isEqualTo(newPerson.getAddressRecords().size());
        assertThat(savedPerson.getContacts().size()).isEqualTo(newPerson.getContacts().size());
        assertThat(savedPerson.getIdentityDocuments().size()).isEqualTo(newPerson.getIdentityDocuments().size());

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
        Person newPerson = simplePersonBuilder()
                .name(null)
                .build();

        assertThatThrownBy(() -> {
            personService.createPerson(newPerson);
            personRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void createPersonWithoutDateOfBirth() {
        Person newPerson = simplePersonBuilder()
                .dateOfBirth(null)
                .build();

        assertThatThrownBy(() -> {
            personService.createPerson(newPerson);
            personRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void createPersonWithoutIsHidden() {
        Person newPerson = simplePersonBuilder()
                .isHidden(null)
                .build();

        assertThatThrownBy(() -> {
            personService.createPerson(newPerson);
            personRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void createPersonWithoutAddresses() {
        Person newPerson = simplePersonBuilder().build();
        newPerson.setAddressRecords(null);
        Person createdPerson = personService.createPerson(newPerson);
        personRepository.flush();
        assertThat(createdPerson.getAddressRecords()).isEmpty();
    }

    @Test
    @Transactional
    void createPersonWithoutContacts() {
        Person newPerson = simplePersonBuilder().build();
        newPerson.setContacts(null);
        Person createdPerson = personService.createPerson(newPerson);
        personRepository.flush();
        assertThat(createdPerson.getContacts()).isNull();
    }

    @Test
    @Transactional
    void createPersonWithoutIdentityDocuments() {
        Person newPerson = simplePersonBuilder().build();
        newPerson.setIdentityDocuments(null);
        assertThatThrownBy(() -> {
            personService.createPerson(newPerson);
            personRepository.flush();
        }).isInstanceOf(PersonCreationException.class);
    }

    @Test
    @Transactional
    void createTwoPeopleWithTheSameDocument() {
        Person p1 = simplePersonBuilder()
                .phoneNumber("+70987654321")
                .build();
        personService.createPerson(p1);

        Person p2 = simplePersonBuilder().build();
        assertThatThrownBy(() -> {
            personService.createPerson(p2);
            personRepository.flush();
        }).isInstanceOf(PersonCreationException.class);
    }

    @Test
    @Transactional
    void createTwoPeopleWithTheSameContact() {
        Person p1 = simplePersonBuilder()
                .documentType(IdentityDocument.DocumentType.PENSION_ID)
                .build();
        personService.createPerson(p1);

        Person p2 = simplePersonBuilder().build();
        assertThatThrownBy(() -> {
            personService.createPerson(p2);
            personRepository.flush();
        }).isInstanceOf(PersonCreationException.class);
    }

    private SimplePersonBuilder simplePersonBuilder() {
        return EntityTestUtil.simplePersonBuilder(regionRepository.findAll().get(0));
    }
}