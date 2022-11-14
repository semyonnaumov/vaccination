package com.naumov.identityservice.service;

import com.naumov.identityservice.EntityTestUtil;
import com.naumov.identityservice.exception.BadInputException;
import com.naumov.identityservice.model.Person;
import com.naumov.identityservice.repository.PersonRepository;
import com.naumov.identityservice.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
public class PersonServiceFindByNameAndDocumentTest {
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
        assertThat(personService.findByNameAndDocument(savedPerson.getName(),
                savedPerson.getIdentityDocuments().get(0).getType().name(),
                savedPerson.getIdentityDocuments().get(0).getFullNumber())).isPresent();
    }

    @Test
    void wrongName() {
        Person person = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(person);

        assertThat(personRepository.count()).isEqualTo(1);

        assertThat(personService.findByNameAndDocument("Wrong name",
                savedPerson.getIdentityDocuments().get(0).getType().name(),
                savedPerson.getIdentityDocuments().get(0).getFullNumber())).isEmpty();
    }

    @Test
    void wrongDoctype() {
        Person person = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(person);

        assertThat(personRepository.count()).isEqualTo(1);
        assertThatThrownBy(() -> personService.findByNameAndDocument(savedPerson.getName(),
                "WRONG_DOC_TYPE",
                savedPerson.getIdentityDocuments().get(0).getFullNumber())).isInstanceOf(BadInputException.class);
    }

    @Test
    void wrongDocNumber() {
        Person person = simplePersonBuilder().build();
        Person savedPerson = personService.createPerson(person);

        assertThat(personRepository.count()).isEqualTo(1);
        assertThat(personService.findByNameAndDocument(savedPerson.getName(),
                savedPerson.getIdentityDocuments().get(0).getType().name(),
                "1111111111")).isEmpty();
    }

    private EntityTestUtil.SimplePersonBuilder simplePersonBuilder() {
        return EntityTestUtil.simplePersonBuilder(regionRepository.findAll().get(0));
    }
}
