package com.naumov.service;

import com.naumov.exception.EntityNotFoundException;
import com.naumov.model.Person;
import com.naumov.model.Region;
import com.naumov.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.naumov.EntityTestUtil.assertThatCollectionsAreNullOrEqualSize;
import static com.naumov.EntityTestUtil.prepareSimplePerson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PersonServiceGetTest {
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
    void getExistingPerson() {
        Region region0 = regionRepository.findAll().get(0);
        Person savedPerson = personService.createPerson(prepareSimplePerson(region0));
        personRepository.flush();
        Person foundPerson = personService.getPerson(savedPerson.getId());

        assertThat(foundPerson).isNotNull();
        assertThat(foundPerson.getName()).isEqualTo(savedPerson.getName());
        assertThat(foundPerson.getDateOfBirth()).isEqualTo(savedPerson.getDateOfBirth());
        assertThat(foundPerson.getIsHidden()).isEqualTo(savedPerson.getIsHidden());
        assertThatCollectionsAreNullOrEqualSize(foundPerson.getAddressRecords(), savedPerson.getAddressRecords());
        assertThatCollectionsAreNullOrEqualSize(foundPerson.getContacts(), savedPerson.getContacts());
        assertThatCollectionsAreNullOrEqualSize(foundPerson.getIdentityDocuments(), savedPerson.getIdentityDocuments());
    }

    @Test
    void getNonExistingPerson() {
        assertThatThrownBy(() -> {
            personService.getPerson(-1L);
            personRepository.flush();
        }).isInstanceOf(EntityNotFoundException.class);
    }
}