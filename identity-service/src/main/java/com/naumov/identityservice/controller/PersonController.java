package com.naumov.identityservice.controller;

import com.naumov.identityservice.dto.DtoConverter;
import com.naumov.identityservice.dto.rq.PersonCreateUpdateRequest;
import com.naumov.identityservice.dto.rs.DefaultErrorResponse;
import com.naumov.identityservice.dto.rs.PersonCreateUpdateResponse;
import com.naumov.identityservice.dto.rs.PersonGetBulkResponse;
import com.naumov.identityservice.dto.rs.PersonGetResponse;
import com.naumov.identityservice.exception.BadInputException;
import com.naumov.identityservice.exception.ResourceNotFoundException;
import com.naumov.identityservice.model.Person;
import com.naumov.identityservice.service.PersonService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/people")
@Validated
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
    public ResponseEntity<PersonCreateUpdateResponse> createPerson(@Valid @RequestBody PersonCreateUpdateRequest rq) {
        Person newPerson = personService.createPerson(dtoConverter.fromPersonCreateUpdateRequest(rq));
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoConverter.toPersonCreateUpdateResponse(newPerson));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonGetResponse> getPerson(@NotNull @PathVariable("id") Long personId) {
        Person person = personService.getPerson(personId);
        return ResponseEntity.status(HttpStatus.OK).body(dtoConverter.toPersonGetResponse(person));
    }

    @GetMapping("/find")
    public ResponseEntity<Long> findPerson(@NotBlank @RequestParam("name") String fullName,
                                           @NotBlank @RequestParam("doc_type") String docType,
                                           @NotBlank @RequestParam("doc_number") String docNumber) {
        Optional<Long> optionalId = personService.findByNameAndDocument(fullName, docType, docNumber);

        return optionalId.map(id -> ResponseEntity.status(HttpStatus.OK).body(id))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping
    public ResponseEntity<PersonCreateUpdateResponse> updatePerson(@Valid @RequestBody PersonCreateUpdateRequest rq) {
        Person updatedPerson = personService.updatePerson(dtoConverter.fromPersonCreateUpdateRequest(rq));
        return ResponseEntity.status(HttpStatus.OK).body(dtoConverter.toPersonCreateUpdateResponse(updatedPerson));
    }

    @GetMapping
    public ResponseEntity<List<PersonGetBulkResponse>> getPeople(@NotNull @PositiveOrZero
                                                                 @RequestParam(name = "page_number") Integer pageNumber,
                                                                 @NotNull @Positive
                                                                 @RequestParam(name = "page_size") Integer pageSize,
                                                                 @RequestParam(required = false) String region) {
        List<Person> people = region != null
                ? personService.getPeople(region, pageNumber, pageSize)
                : personService.getPeople(pageNumber, pageSize);

        List<PersonGetBulkResponse> body = people.stream()
                .map(dtoConverter::toPersonGetBulkResponse)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @ExceptionHandler({BadInputException.class, HttpMessageNotReadableException.class})
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

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<DefaultErrorResponse> handleValidationExceptions(Exception e) {
        LOGGER.error("Bad request, returning {}", HttpStatus.BAD_REQUEST, e);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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
