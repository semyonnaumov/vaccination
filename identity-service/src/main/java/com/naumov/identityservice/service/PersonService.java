package com.naumov.identityservice.service;

import com.naumov.identityservice.model.Person;

import java.util.List;

public interface PersonService {
    Person createPerson(Person person);

    Person getPerson(long personId);

    List<Person> getPeople(String region, int pageNumber, int pageSize);

    List<Person> getPeople(int pageNumber, int pageSize);

    Person updatePerson(Person person);

    boolean verifyPassport(String fullName, String passport);
}
