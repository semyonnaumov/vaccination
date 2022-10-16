package com.naumov.service;

import com.naumov.model.Person;

import java.util.List;

public interface PersonService {
    Person createPerson(Person person);

    Person updatePerson(Person person);

    Person getPerson(long personId);

    List<Person> getPeople(String region, int pageNumber, int pageSize);

    List<Person> getPeople(int pageNumber, int pageSize);

    boolean verifyPassport(String fullName, String passport);
}
