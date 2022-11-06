package com.naumov.controller;

import com.naumov.dto.DtoConverter;
import com.naumov.dto.rq.PersonCreateUpdateRequest;
import com.naumov.dto.rs.DefaultErrorResponse;
import com.naumov.dto.rs.PersonCreateUpdateResponse;
import com.naumov.dto.rs.PersonGetBulkResponse;
import com.naumov.dto.rs.PersonGetResponse;
import com.naumov.exception.ResourceManipulationException;
import com.naumov.exception.ResourceNotFoundException;
import com.naumov.model.Person;
import com.naumov.service.PersonService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
    private static final Logger LOGGER = LogManager.getLogger(PersonController.class);

    private final PersonService personService;
    private final DtoConverter dtoConverter;

    @Autowired
    public PersonController(PersonService personService, DtoConverter dtoConverter) {
        this.personService = personService;
        this.dtoConverter = dtoConverter;
    }

    @PostMapping
    public ResponseEntity<PersonCreateUpdateResponse> createPerson(@Valid @RequestBody PersonCreateUpdateRequest personCreateRequest) {
        Person newPerson = personService.createPerson(dtoConverter.fromPersonCreateUpdateRequest(personCreateRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoConverter.toPersonCreateUpdateResponse(newPerson));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonGetResponse> getPerson(@Valid @NotNull @PathVariable("id") Long personId) {
        Person person = personService.getPerson(personId);
        return ResponseEntity.status(HttpStatus.OK).body(dtoConverter.toPersonGetResponse(person));
    }

    @PutMapping
    public ResponseEntity<PersonCreateUpdateResponse> updatePerson(@Valid @RequestBody PersonCreateUpdateRequest personUpdateRequest) {
        Person updatedPerson = personService.updatePerson(dtoConverter.fromPersonCreateUpdateRequest(personUpdateRequest));
        return ResponseEntity.status(HttpStatus.OK).body(dtoConverter.toPersonCreateUpdateResponse(updatedPerson));
    }

    // TODO add test
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

    // TODO add test
    @GetMapping("/verify")
    public boolean verifyPerson(@Valid @NotNull @RequestParam("name") String fullName,
                                @Valid @NotNull @RequestParam("passport") String passportNumber) {
        return personService.verifyPassport(fullName, passportNumber);
    }

    @ExceptionHandler({ResourceManipulationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<DefaultErrorResponse> handleBadRequest(Exception e) {
        LOGGER.error("Bad request, returning {}", HttpStatus.BAD_REQUEST, e);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .headers(httpHeaders)
                .body(new DefaultErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<DefaultErrorResponse> handleNotFound(Exception e) {
        LOGGER.error("Not found, returning {}", HttpStatus.NOT_FOUND, e);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .headers(httpHeaders)
                .body(new DefaultErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<DefaultErrorResponse> handleAllOtherExceptions(Exception e) {
        LOGGER.error("General exception handling, returning {}", HttpStatus.INTERNAL_SERVER_ERROR, e);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .headers(httpHeaders)
                .body(new DefaultErrorResponse(e.getMessage()));
    }
}
