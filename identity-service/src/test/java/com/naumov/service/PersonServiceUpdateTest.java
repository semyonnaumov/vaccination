package com.naumov.service;

import com.naumov.EntityTestUtil;
import com.naumov.exception.ResourceConflictException;
import com.naumov.exception.ResourceCreationException;
import com.naumov.exception.ResourceNotFoundException;
import com.naumov.exception.ResourceUpdateException;
import com.naumov.model.IdentityDocument;
import com.naumov.model.Person;
import com.naumov.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.naumov.EntityTestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// TODO
@SpringBootTest
@Transactional
class PersonServiceUpdateTest {
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
    void updateEntityWithoutId() {
        Person updatedPerson = simplePersonBuilder().build();
        assertThatThrownBy(() -> personService.updatePerson(updatedPerson)).isInstanceOf(ResourceUpdateException.class);
    }

    @Test
    void regularSimpleUpdate() {
        Person newPerson = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(newPerson);

        assertReposStateIsValid();

        Long id = savedPerson.getId();
        Person updatedPerson = simplePersonBuilder()
                .id(id)
                .contactId(savedPerson.getContacts().get(0).getId())
                .identityDocumentId(savedPerson.getIdentityDocuments().get(0).getId())
                .addressRecordId(savedPerson.getAddressRecords().get(0).getId())
                .addressId(savedPerson.getAddressRecords().get(0).getAddress().getId())
                .name("Updated name")
                .dateOfBirth(LocalDate.MAX)
                .build();

        personService.updatePerson(updatedPerson);

        assertThat(personRepository.count()).isEqualTo(1);
        assertThat(addressRepository.count()).isEqualTo(1);
        assertThat(contactRepository.count()).isEqualTo(1);
        assertThat(identityDocumentRepository.count()).isEqualTo(1);
        assertThat(personAddressRepository.count()).isEqualTo(1);

        Person person = personRepository.getReferenceById(id);
        assertThat(person.getName()).isEqualTo("Updated name");
        assertThat(person.getDateOfBirth()).isEqualTo(LocalDate.MAX);
    }

    @Test
    void addContactAndDocument() {
        Person newPerson = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(newPerson);

        assertReposStateIsValid();

        Long id = savedPerson.getId();
        Person updatedPerson = simplePersonBuilder()
                .id(id)
                .contactId(savedPerson.getContacts().get(0).getId())
                .identityDocumentId(savedPerson.getIdentityDocuments().get(0).getId())
                .addressRecordId(savedPerson.getAddressRecords().get(0).getId())
                .addressId(savedPerson.getAddressRecords().get(0).getAddress().getId())
                .build();

        addContact(updatedPerson, "+70987654321");
        addIdentityDocument(updatedPerson, IdentityDocument.DocumentType.MEDICAL_INSURANCE, "00000", "2000-12-12", false);

        personService.updatePerson(updatedPerson);

        assertThat(personRepository.count()).isEqualTo(1);
        assertThat(addressRepository.count()).isEqualTo(1);
        assertThat(contactRepository.count()).isEqualTo(2);
        assertThat(identityDocumentRepository.count()).isEqualTo(2);
        assertThat(personAddressRepository.count()).isEqualTo(1);

        Person person = personRepository.getReferenceById(id);
        assertThat(person.getContacts().size()).isEqualTo(2);
        assertThat(person.getIdentityDocuments().size()).isEqualTo(2);
    }

    @Test
    void addContactWithUnknownId() {
        Person newPerson = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(newPerson);

        assertReposStateIsValid();

        Long id = savedPerson.getId();
        Person updatedPerson = simplePersonBuilder()
                .id(id)
                .contactId(savedPerson.getContacts().get(0).getId())
                .identityDocumentId(savedPerson.getIdentityDocuments().get(0).getId())
                .addressRecordId(savedPerson.getAddressRecords().get(0).getId())
                .addressId(savedPerson.getAddressRecords().get(0).getAddress().getId())
                .build();

        addContact(-1L, updatedPerson, "+70987654321");

        assertThatThrownBy(() -> personService.updatePerson(updatedPerson)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addContactWithSamePhoneNumber() {
        Person newPerson = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(newPerson);

        assertReposStateIsValid();

        Long id = savedPerson.getId();
        Person updatedPerson = simplePersonBuilder()
                .id(id)
                .contactId(savedPerson.getContacts().get(0).getId())
                .identityDocumentId(savedPerson.getIdentityDocuments().get(0).getId())
                .addressRecordId(savedPerson.getAddressRecords().get(0).getId())
                .addressId(savedPerson.getAddressRecords().get(0).getAddress().getId())
                .build();

        addContact(updatedPerson, savedPerson.getContacts().get(0).getPhoneNumber());

        assertThatThrownBy(() -> personService.updatePerson(updatedPerson)).isInstanceOf(ResourceCreationException.class);
    }

    @Test
    void addIdentityDocumentWithUnknownId() {
        Person newPerson = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(newPerson);

        assertReposStateIsValid();

        Long id = savedPerson.getId();
        Person updatedPerson = simplePersonBuilder()
                .id(id)
                .contactId(savedPerson.getContacts().get(0).getId())
                .identityDocumentId(savedPerson.getIdentityDocuments().get(0).getId())
                .addressRecordId(savedPerson.getAddressRecords().get(0).getId())
                .addressId(savedPerson.getAddressRecords().get(0).getAddress().getId())
                .build();

        addIdentityDocument(-1L, updatedPerson, IdentityDocument.DocumentType.MEDICAL_INSURANCE, "00000", "2000-12-12", false);

        assertThatThrownBy(() -> personService.updatePerson(updatedPerson)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addNonUniqueIdentityDocument() {
        Person newPerson = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(newPerson);

        assertReposStateIsValid();

        Long id = savedPerson.getId();
        Person updatedPerson = simplePersonBuilder()
                .id(id)
                .contactId(savedPerson.getContacts().get(0).getId())
                .identityDocumentId(savedPerson.getIdentityDocuments().get(0).getId())
                .addressRecordId(savedPerson.getAddressRecords().get(0).getId())
                .addressId(savedPerson.getAddressRecords().get(0).getAddress().getId())
                .build();

        addIdentityDocument(updatedPerson, IdentityDocument.DocumentType.INNER_PASSPORT, "12345", "1999-12-12", false);

        assertThatThrownBy(() -> personService.updatePerson(updatedPerson)).isInstanceOf(ResourceCreationException.class);
    }

    @Test
    void addSecondPrimaryIdentityDocument() {
        Person newPerson = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(newPerson);

        assertReposStateIsValid();

        Long id = savedPerson.getId();
        Person updatedPerson = simplePersonBuilder()
                .id(id)
                .contactId(savedPerson.getContacts().get(0).getId())
                .identityDocumentId(savedPerson.getIdentityDocuments().get(0).getId())
                .addressRecordId(savedPerson.getAddressRecords().get(0).getId())
                .addressId(savedPerson.getAddressRecords().get(0).getAddress().getId())
                .build();

        addIdentityDocument(updatedPerson, IdentityDocument.DocumentType.INNER_PASSPORT, "22222", "1999-12-12", true);

        assertThatThrownBy(() -> personService.updatePerson(updatedPerson)).isInstanceOf(ResourceConflictException.class);
    }

    @Test
    void deletedAllAddresses() {
        Person newPerson = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(newPerson);

        assertReposStateIsValid();

        Long id = savedPerson.getId();
        Person updatedPerson = simplePersonBuilder()
                .id(id)
                .contactId(savedPerson.getContacts().get(0).getId())
                .identityDocumentId(savedPerson.getIdentityDocuments().get(0).getId())
                .build();

        updatedPerson.setAddressRecords(null);

        assertThatThrownBy(() -> personService.updatePerson(updatedPerson)).isInstanceOf(ResourceConflictException.class);
    }

    // TODO add addresses tests

    private void assertReposStateIsValid() {
        assertThat(personRepository.count()).isEqualTo(1);
        assertThat(addressRepository.count()).isEqualTo(1);
        assertThat(contactRepository.count()).isEqualTo(1);
        assertThat(identityDocumentRepository.count()).isEqualTo(1);
        assertThat(personAddressRepository.count()).isEqualTo(1);
    }

//    @Test
//    @Transactional
//    void createPersonWithOneExistingAddress() {
//        Region region0 = regionRepository.findAll().get(0);
//        Region region1 = regionRepository.findAll().get(1);
//
//        // prepare DB
//        Person transientExistingPerson = Person.builder()
//                .name("Person 0")
//                .dateOfBirth(LocalDate.EPOCH)
//                .isHidden(false)
//                .build();
//        addIdentityDocument(transientExistingPerson, IdentityDocument.DocumentType.INNER_PASSPORT, "12345", "1999-12-12", true);
//        addAddressRecord(transientExistingPerson, region0, "Address line 0", true);
//        Person existingPerson = personService.createPerson(transientExistingPerson);
//
//        // test adding new person with existing address
//        Person newPerson = Person.builder()
//                .name("Person 1")
//                .dateOfBirth(LocalDate.EPOCH)
//                .isHidden(true)
//                .build();
//        addIdentityDocument(newPerson, IdentityDocument.DocumentType.INTERNATIONAL_PASSPORT, "0123401234", "2001-01-01", true);
//        addAddressRecord(newPerson, region0, "Address line 0", false);
//        addAddressRecord(newPerson, region1, "Address line 1", true);
//        Person savedPerson = personService.createPerson(newPerson);
//
//        personRepository.flush();
//
//        // assertions
//        assertThat(existingPerson.getAddressRecords().size()).isEqualTo(1);
//        assertThat(savedPerson.getAddressRecords().size()).isEqualTo(2);
//        assertThat(addressRepository.count()).isEqualTo(2);
//        assertThat(personAddressRepository.count()).isEqualTo(3);
//    }

    private SimplePersonBuilder simplePersonBuilder() {
        return EntityTestUtil.simplePersonBuilder(regionRepository.findAll().get(0));
    }
}