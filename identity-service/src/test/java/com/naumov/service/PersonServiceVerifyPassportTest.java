package com.naumov.service;

import com.naumov.EntityTestUtil;
import com.naumov.model.Person;
import com.naumov.repository.PersonRepository;
import com.naumov.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
public class PersonServiceVerifyPassportTest {
    @Autowired
    PersonService personService;
    @Autowired
    PersonRepository personRepository;
    @Autowired
    RegionRepository regionRepository;

    @Test
    void positive() {
        Person person = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(person);

        assertThat(personRepository.count()).isEqualTo(1);

        assertThat(personService.verifyPassport(savedPerson.getName(),
                savedPerson.getIdentityDocuments().get(0).getFullNumber())).isTrue();
    }

    @Test
    void wrongPassport() {
        Person person = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(person);

        assertThat(personRepository.count()).isEqualTo(1);

        assertThat(personService.verifyPassport(savedPerson.getName(),
                "1111111111")).isFalse();
    }

    @Test
    void wrongName() {
        Person person = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(person);

        assertThat(personRepository.count()).isEqualTo(1);

        assertThat(personService.verifyPassport("Wrong name",
                savedPerson.getIdentityDocuments().get(0).getFullNumber())).isFalse();
    }

    private EntityTestUtil.SimplePersonBuilder simplePersonBuilder() {
        return EntityTestUtil.simplePersonBuilder(regionRepository.findAll().get(0));
    }
}
