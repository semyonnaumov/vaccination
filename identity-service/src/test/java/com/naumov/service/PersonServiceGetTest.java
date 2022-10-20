package com.naumov.service;

import com.naumov.exception.EntityNotFoundException;
import com.naumov.model.Person;
import com.naumov.model.Region;
import com.naumov.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.naumov.EntityTestUtil.assertThatCollectionsAreNullOrEqualSize;
import static com.naumov.EntityTestUtil.simplePersonBuilder;
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
        Person savedPerson = personService.createPerson(simplePersonBuilder(region0).build());
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

    @Test
    void testGetPeopleByRegion() {
        List<Region> allRegions = regionRepository.findAll();
        Region region0 = allRegions.get(0);
        Region region1 = allRegions.get(1);
        Region region2 = allRegions.get(2);

        // create 3 people in region 0:
        Person person00 = simplePersonBuilder(region0)
                .name("person00")
                .phoneNumber("+70000000000")
                .documentFullNumber("00000")
                .addressString("Address 00")
                .build();

        Person person01 = simplePersonBuilder(region0)
                .name("person01")
                .phoneNumber("+70111111111")
                .documentFullNumber("01111")
                .addressString("Address 01")
                .build();

        Person person02 = simplePersonBuilder(region0)
                .name("person02")
                .phoneNumber("+70222222222")
                .documentFullNumber("02222")
                .addressString("Address 02")
                .build();

        personService.createPerson(person00);
        personService.createPerson(person01);
        personService.createPerson(person02);

        // create 1 person in region 1:
        Person person10 = simplePersonBuilder(region1)
                .name("person10")
                .phoneNumber("+71000000000")
                .documentFullNumber("10000")
                .addressString("Address 10")
                .build();

        personService.createPerson(person10);

        personRepository.flush();
        assertThat(personRepository.count()).isEqualTo(4);

        // test search by region
        List<Person> people00 = personService.getPeople(region0.getName(), 0, 2);
        assertThat(people00.size()).isEqualTo(2);
        assertThat(people00).allMatch(p -> p.getContacts() != null && p.getContacts().size() == 1);
        assertThat(people00).allMatch(p -> p.getIdentityDocuments() != null && p.getIdentityDocuments().size() == 1);
        assertThat(people00).allMatch(p -> p.getAddressRecords() != null && p.getAddressRecords().size() == 1);
        assertThat(people00).allMatch(p -> p.getAddressRecords().get(0) != null && p.getAddressRecords().get(0).getAddress() != null);

        List<Person> people01 = personService.getPeople(region0.getName(), 1, 2);
        assertThat(people01.size()).isEqualTo(1);

        List<Person> people02 = personService.getPeople(region0.getName(), 2, 2);
        assertThat(people02.size()).isEqualTo(0);

        List<Person> people03 = personService.getPeople(region0.getName(), 0, 5);
        assertThat(people03.size()).isEqualTo(3);

        List<Person> people20 = personService.getPeople(region2.getName(), 0, 5);
        assertThat(people20.size()).isEqualTo(0);

        // test general search
        List<Person> people30 = personService.getPeople(0, 3);
        assertThat(people30.size()).isEqualTo(3);

        List<Person> people31 = personService.getPeople(1, 3);
        assertThat(people31.size()).isEqualTo(1);

        List<Person> people32 = personService.getPeople(2, 3);
        assertThat(people32.size()).isEqualTo(0);

        List<Person> people33 = personService.getPeople(0, 5);
        assertThat(people33.size()).isEqualTo(4);
    }
}