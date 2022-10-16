package com.naumov.dto;

import com.naumov.dto.rq.*;
import com.naumov.dto.rs.*;
import com.naumov.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class DtoConverter {

    // -------------------------------------------- "From" mappings ----------------------------------------------------
    public Person fromPersonCreateRequest(PersonCreateRequest personCreateRequest) {
        if (personCreateRequest == null) return null;
        List<AddressCreateRequest> addressCreateRequests = personCreateRequest.getAddresses();
        List<ContactCreateRequest> contactCreateRequests = personCreateRequest.getContacts();
        List<IdentityDocumentCreateRequest> identityDocumentCreateRequests = personCreateRequest.getIdentityDocuments();

        // not using builder because some fields need backreference to the person instance
        Person person = new Person();
        person.setName(personCreateRequest.getName());
        person.setDateOfBirth(personCreateRequest.getDateOfBirth());
        person.setIsHidden(personCreateRequest.getIsHidden());
        person.setAddressRecords(mapItems(addressCreateRequests, e -> fromAddressCreateRequest(e, person))); // many-to-many mapping, be careful
        person.setContacts(mapItems(contactCreateRequests, e -> fromContactCreateRequest(e, person)));
        person.setIdentityDocuments(mapItems(identityDocumentCreateRequests, e -> fromIdentityDocumentCreateRequest(e, person)));

        return person;
    }

    public PersonAddress fromAddressCreateRequest(AddressCreateRequest addressCreateRequest, Person person) {
        if (addressCreateRequest == null) return null;

        Address address = Address.builder()
                .region(Region.builder().name(addressCreateRequest.getRegion()).build())
                .address(addressCreateRequest.getAddress())
                .build();

        PersonAddress personAddress = PersonAddress.builder()
                .person(person)
                .address(address)
                .isRegistration(addressCreateRequest.getIsRegistrationAddress())
                .build();

        // be careful - this address might already have
        // more than one person records in DB
        // whether it has an id or not
        address.setPersonRecords(List.of(personAddress));

        return personAddress;
    }

    public Contact fromContactCreateRequest(ContactCreateRequest contactCreateRequest, Person person) {
        if (contactCreateRequest == null) return null;
        return Contact.builder()
                .owner(person)
                .phoneNumber(contactCreateRequest.getPhoneNumber())
                .build();
    }

    public IdentityDocument fromIdentityDocumentCreateRequest(IdentityDocumentCreateRequest identityDocumentCreateRequest,
                                                              Person person) {
        if (identityDocumentCreateRequest == null) return null;
        return IdentityDocument.builder()
                .type(IdentityDocument.DocumentType.valueOf(identityDocumentCreateRequest.getType()))
                .fullNumber(identityDocumentCreateRequest.getFullNumber())
                .issueDate(identityDocumentCreateRequest.getIssueDate())
                .owner(person)
                .isPrimary(identityDocumentCreateRequest.getIsPrimary())
                .build();
    }

    /* TODO Maybe create base interface for PersonUpdateRequest and PersonCreateRequest and use one method
        instead of copy-pasting? This can apply to the 3 inner methods. */
    public Person fromPersonCreateRequest(PersonUpdateRequest personUpdateRequest) {
        if (personUpdateRequest == null) return null;
        List<AddressUpdateRequest> addressUpdateRequests = personUpdateRequest.getAddresses();
        List<ContactUpdateRequest> contactUpdateRequests = personUpdateRequest.getContacts();
        List<IdentityDocumentUpdateRequest> identityDocumentUpdateRequests = personUpdateRequest.getIdentityDocuments();

        // not using builder because some fields need backreference to the person instance
        Person person = new Person();
        person.setId(personUpdateRequest.getId());
        person.setName(personUpdateRequest.getName());
        person.setDateOfBirth(personUpdateRequest.getDateOfBirth());
        person.setIsHidden(personUpdateRequest.getIsHidden());
        person.setAddressRecords(mapItems(addressUpdateRequests, e -> fromAddressUpdateRequest(e, person))); // many-to-many mapping, be careful
        person.setContacts(mapItems(contactUpdateRequests, e -> fromContactUpdateRequest(e, person)));
        person.setIdentityDocuments(mapItems(identityDocumentUpdateRequests, e -> fromIdentityDocumentUpdateRequest(e, person)));

        return person;
    }

    private PersonAddress fromAddressUpdateRequest(AddressUpdateRequest addressUpdateRequest, Person person) {
        if (addressUpdateRequest == null) return null;

        Address address = Address.builder()
                .id(addressUpdateRequest.getId())
                .region(Region.builder().name(addressUpdateRequest.getRegion()).build())
                .address(addressUpdateRequest.getAddress())
                .build();

        PersonAddress personAddress = PersonAddress.builder()
                .person(person)
                .address(address)
                .isRegistration(addressUpdateRequest.getIsRegistrationAddress())
                .build();

        // be careful - this address might already have
        // more than one person records in DB
        // whether it has an id or not
        address.setPersonRecords(List.of(personAddress));

        return personAddress;
    }

    private Contact fromContactUpdateRequest(ContactUpdateRequest contactUpdateRequest, Person person) {
        if (contactUpdateRequest == null) return null;
        return Contact.builder()
                .id(contactUpdateRequest.getId())
                .owner(person)
                .phoneNumber(contactUpdateRequest.getPhoneNumber())
                .build();
    }

    private IdentityDocument fromIdentityDocumentUpdateRequest(IdentityDocumentUpdateRequest identityDocumentUpdateRequest,
                                                               Person person) {
        if (identityDocumentUpdateRequest == null) return null;
        return IdentityDocument.builder()
                .id(identityDocumentUpdateRequest.getId())
                .type(IdentityDocument.DocumentType.valueOf(identityDocumentUpdateRequest.getType()))
                .fullNumber(identityDocumentUpdateRequest.getFullNumber())
                .issueDate(identityDocumentUpdateRequest.getIssueDate())
                .owner(person)
                .isPrimary(identityDocumentUpdateRequest.getIsPrimary())
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
