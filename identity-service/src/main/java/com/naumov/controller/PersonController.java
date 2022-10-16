package com.naumov.controller;

import com.naumov.dto.DtoConverter;
import com.naumov.dto.rq.PersonCreateRequest;
import com.naumov.dto.rq.PersonUpdateRequest;
import com.naumov.dto.rs.PersonCreateUpdateResponse;
import com.naumov.dto.rs.PersonGetBulkResponse;
import com.naumov.dto.rs.PersonGetResponse;
import com.naumov.model.Person;
import com.naumov.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/people")
public class PersonController {
    private final PersonService personService;
    private final DtoConverter dtoConverter;

    @Autowired
    public PersonController(PersonService personService, DtoConverter dtoConverter) {
        this.personService = personService;
        this.dtoConverter = dtoConverter;
    }

    @PostMapping
    public ResponseEntity<PersonCreateUpdateResponse> createPerson(@Valid @RequestBody PersonCreateRequest personCreateRequest) {
        Person newPerson = personService.createPerson(dtoConverter.fromPersonCreateRequest(personCreateRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoConverter.toPersonCreateUpdateResponse(newPerson));
    }

    // return 200 or 201?
    @PutMapping
    public ResponseEntity<PersonCreateUpdateResponse> updatePerson(@Valid @RequestBody PersonUpdateRequest personUpdateRequest) {
        Person updatedPerson = personService.updatePerson(dtoConverter.fromPersonCreateRequest(personUpdateRequest));
        return ResponseEntity.status(HttpStatus.OK).body(dtoConverter.toPersonCreateUpdateResponse(updatedPerson));
    }

    // return 404 if not found
    @GetMapping("/{id}")
    public ResponseEntity<PersonGetResponse> getPerson(@Valid @NotNull @PathVariable("id") Long personId) {
        Person person = personService.getPerson(personId);
        return ResponseEntity.status(HttpStatus.OK).body(dtoConverter.toPersonGetResponse(person));
    }

    @GetMapping
    public ResponseEntity<List<PersonGetBulkResponse>> getPeople(@Valid
                                                                 @NotNull
                                                                 @PositiveOrZero
                                                                 @RequestParam Integer pageNumber,
                                                                 @Valid
                                                                 @NotNull
                                                                 @Positive
                                                                 @RequestParam Integer pageSize,
                                                                 @RequestParam(name = "region") Optional<String> region) {
        List<Person> people = region.isPresent()
                ? personService.getPeople(region.get(), pageNumber, pageSize)
                : personService.getPeople(pageNumber, pageSize);

        List<PersonGetBulkResponse> body = people.stream()
                .map(dtoConverter::toPersonGetBulkResponse)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping("/verify")
    public boolean verifyPerson(@Valid @NotNull @RequestParam("name") String fullName,
                                @Valid @NotNull @RequestParam("passport") String passportNumber) {
        return personService.verifyPassport(fullName, passportNumber);
    }
}