package com.naumov.dto;

import com.naumov.dto.rq.AddressCreateUpdateRequest;
import com.naumov.dto.rq.ContactCreateUpdateRequest;
import com.naumov.dto.rq.IdentityDocumentCreateUpdateRequest;
import com.naumov.dto.rq.PersonCreateUpdateRequest;
import com.naumov.dto.rs.*;
import com.naumov.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class DtoConverter {

    // -------------------------------------------- "From" mappings ----------------------------------------------------
    public Person fromPersonCreateUpdateRequest(PersonCreateUpdateRequest personRequest) {
        if (personRequest == null) return null;
        List<AddressCreateUpdateRequest> addressUpdateRequests = personRequest.getAddresses();
        List<ContactCreateUpdateRequest> contactUpdateRequests = personRequest.getContacts();
        List<IdentityDocumentCreateUpdateRequest> identityDocumentUpdateRequests = personRequest.getIdentityDocuments();

        // not using builder because some fields need backreference to the person instance
        Person person = new Person();
        person.setId(personRequest.getId());
        person.setName(personRequest.getName());
        person.setDateOfBirth(personRequest.getDateOfBirth());
        person.setIsHidden(personRequest.getIsHidden());
        person.setAddressRecords(mapItems(addressUpdateRequests, e -> fromAddressCreateUpdateRequest(e, person))); // many-to-many mapping, be careful
        person.setContacts(mapItems(contactUpdateRequests, e -> fromContactCreateUpdateRequest(e, person)));
        person.setIdentityDocuments(mapItems(identityDocumentUpdateRequests, e -> fromIdentityDocumentCreateUpdateRequest(e, person)));

        return person;
    }

    private PersonAddress fromAddressCreateUpdateRequest(AddressCreateUpdateRequest addressRequest, Person person) {
        if (addressRequest == null) return null;

        Address address = Address.builder()
                .id(addressRequest.getId())
                .region(Region.builder().name(addressRequest.getRegion()).build())
                .address(addressRequest.getAddress())
                .build();

        PersonAddress personAddress = PersonAddress.builder()
                .person(person)
                .address(address)
                .isRegistration(addressRequest.getIsRegistrationAddress())
                .build();

        // be careful - this address might already have
        // more than one person records in DB
        // whether it has an id or not
        List<PersonAddress> personRecords = new ArrayList<>();
        personRecords.add(personAddress);
        address.setPersonRecords(personRecords);

        return personAddress;
    }

    private Contact fromContactCreateUpdateRequest(ContactCreateUpdateRequest contactRequest, Person person) {
        if (contactRequest == null) return null;
        return Contact.builder()
                .id(contactRequest.getId())
                .owner(person)
                .phoneNumber(contactRequest.getPhoneNumber())
                .build();
    }

    private IdentityDocument fromIdentityDocumentCreateUpdateRequest(IdentityDocumentCreateUpdateRequest identityDocumentRequest,
                                                                     Person person) {
        if (identityDocumentRequest == null) return null;
        return IdentityDocument.builder()
                .id(identityDocumentRequest.getId())
                .type(IdentityDocument.DocumentType.valueOf(identityDocumentRequest.getType()))
                .fullNumber(identityDocumentRequest.getFullNumber())
                .issueDate(identityDocumentRequest.getIssueDate())
                .owner(person)
                .isPrimary(identityDocumentRequest.getIsPrimary())
                .build();
    }

    // --------------------------------------------- "To" mappings -----------------------------------------------------

    public PersonCreateUpdateResponse toPersonCreateUpdateResponse(Person person) {
        if (person == null) return null;
        List<PersonAddress> addresses = person.getAddressRecords();
        List<IdentityDocument> identityDocuments = person.getIdentityDocuments();
        List<Contact> contacts = person.getContacts();

        return PersonCreateUpdateResponse.builder()
                .id(person.getId())
                .isHidden(person.getIsHidden())
                .addresses(mapItems(addresses, this::toAddressCreateUpdateResponse))
                .contacts(mapItems(contacts, this::toContactCreateUpdateResponse))
                .identityDocuments(mapItems(identityDocuments, this::toIdentityDocumentCreateUpdateResponse))
                .build();
    }

    private AddressCreateUpdateResponse toAddressCreateUpdateResponse(PersonAddress personAddress) {
        if (personAddress == null || personAddress.getAddress() == null) return null;
        return AddressCreateUpdateResponse.builder()
                .id(personAddress.getAddress().getId())
                .build();
    }

    private ContactCreateUpdateResponse toContactCreateUpdateResponse(Contact contact) {
        if (contact == null) return null;
        return ContactCreateUpdateResponse.builder()
                .id(contact.getId())
                .build();
    }

    private IdentityDocumentCreateUpdateResponse toIdentityDocumentCreateUpdateResponse(IdentityDocument identityDocument) {
        if (identityDocument == null) return null;
        return IdentityDocumentCreateUpdateResponse.builder()
                .id(identityDocument.getId())
                .build();
    }

    public PersonGetResponse toPersonGetResponse(Person person) {
        if (person == null) return null;
        List<PersonAddress> addresses = person.getAddressRecords();
        List<IdentityDocument> identityDocuments = person.getIdentityDocuments();
        List<Contact> contacts = person.getContacts();

        return PersonGetResponse.builder()
                .id(person.getId())
                .name(person.getName())
                .dateOfBirth(person.getDateOfBirth())
                .isHidden(person.getIsHidden())
                .addresses(mapItems(addresses, this::toAddressGetResponse))
                .contacts(mapItems(contacts, this::toContactGetResponse))
                .identityDocuments(mapItems(identityDocuments, this::toIdentityDocumentGetResponse))
                .build();
    }

    private AddressGetResponse toAddressGetResponse(PersonAddress personAddress) {
        if (personAddress == null || personAddress.getAddress() == null) return null;
        Address address = personAddress.getAddress();
        return AddressGetResponse.builder()
                .id(address.getId())
                .region(address.getRegion().getName())
                .address(address.getAddress())
                .isRegistrationAddress(personAddress.getIsRegistration())
                .build();
    }

    private ContactGetResponse toContactGetResponse(Contact contact) {
        if (contact == null) return null;
        return ContactGetResponse.builder()
                .id(contact.getId())
                .phoneNumber(contact.getPhoneNumber())
                .build();
    }

    private IdentityDocumentGetResponse toIdentityDocumentGetResponse(IdentityDocument identityDocument) {
        if (identityDocument == null) return null;
        return IdentityDocumentGetResponse.builder()
                .id(identityDocument.getId())
                .type(identityDocument.getType().name())
                .fullNumber(identityDocument.getFullNumber())
                .issueDate(identityDocument.getIssueDate())
                .isPrimary(identityDocument.getIsPrimary())
                .build();
    }

    public PersonGetBulkResponse toPersonGetBulkResponse(Person person) {
        if (person == null) return null;
        List<PersonAddress> addresses = person.getAddressRecords();
        List<IdentityDocument> identityDocuments = person.getIdentityDocuments();
        List<Contact> contacts = person.getContacts();

        return PersonGetBulkResponse.builder()
                .id(person.getId())
                .name(person.getName())
                .dateOfBirth(person.getDateOfBirth())
                .contact(findAndMapItem(contacts, e -> true, this::toContactGetResponse))
                .mainIdentityDocument(findAndMapItem(identityDocuments, IdentityDocument::getIsPrimary, this::toIdentityDocumentGetResponse))
                .registrationAddress(findAndMapItem(addresses, PersonAddress::getIsRegistration, this::toAddressGetResponse))
                .build();
    }

    // --------------------------------------------------- Helpers -----------------------------------------------------

    private <S, D> List<D> mapItems(List<S> items, Function<S, D> mapper) {
        if (items == null) return new ArrayList<>();
        return items.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    private <S, D> D findAndMapItem(List<S> items, Predicate<S> filter, Function<S, D> mapper) {
        if (items == null) return null;
        return items.stream()
                .filter(filter)
                .filter(Objects::nonNull)
                .map(mapper)
                .findFirst().orElse(null);
    }
}
