package com.naumov.identityservice.service;

import com.naumov.identityservice.model.Person;

import java.util.List;
import java.util.Optional;

public interface PersonService {
    Person createPerson(Person person);

    Person getPerson(long personId);

    List<Person> getPeople(String region, int pageNumber, int pageSize);

    List<Person> getPeople(int pageNumber, int pageSize);

    Person updatePerson(Person person);

    Optional<Long> findByNameAndDocument(String fullName, String docType, String docNumber);
}
