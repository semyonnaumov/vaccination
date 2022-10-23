package com.naumov.service.impl;

import com.naumov.exception.ResourceConflictException;
import com.naumov.exception.ResourceCreationException;
import com.naumov.exception.ResourceNotFoundException;
import com.naumov.exception.ResourceUpdateException;
import com.naumov.model.*;
import com.naumov.repository.AddressRepository;
import com.naumov.repository.ContactRepository;
import com.naumov.repository.IdentityDocumentRepository;
import com.naumov.repository.PersonRepository;
import com.naumov.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PersonServiceImpl implements PersonService {
    private final PersonRepository personRepository;
    private final AddressRepository addressRepository;
    private final ContactRepository contactRepository;
    private final IdentityDocumentRepository identityDocumentRepository;

    @Autowired
    public PersonServiceImpl(PersonRepository personRepository,
                             AddressRepository addressRepository,
                             ContactRepository contactRepository,
                             IdentityDocumentRepository identityDocumentRepository) {
        this.personRepository = personRepository;
        this.addressRepository = addressRepository;
        this.contactRepository = contactRepository;
        this.identityDocumentRepository = identityDocumentRepository;
    }

    /*
     * Person creation scenario:
     * We cannot save everything cascadely since some addresses may already exist, and the request will end
     * up with unique constraint violation exception. Hence, we need to process addresses separately.
     *
     * 1. Extract all addresses from transient person entity
     *    -> list of transient Address entities
     * 2. Validate the person has exactly one registration address
     * 3. Load all existing Address entities, save all new address entities. While saving the Address,
     *    associated PersonAddress entities will not be updated, since we haven't saved the Person yet.
     *    -> list of saved Address entities
     * 3. Re-create a list of PersonAddress entities from saved Address records, add them to Person ->
     *    -> transient Person ready to be saved
     * 4. Save the Person entity. PersonAddress,Contact and IdentityDocument entities will be saved cascadely.
     *    Unique constraint violation exceptions (phone # and ID) may occur - the transaction will be rolled back.
     * */
    @Override
    @Transactional
    public Person createPerson(Person newPerson) {
        if (newPerson == null) throw new ResourceCreationException("Created person cannot be null");

        validateIdentityDocumentsForCreation(newPerson.getIdentityDocuments());
        validateContactsForCreation(newPerson.getContacts());

        List<PersonAddress> newAddressRecords = newPerson.getAddressRecords();
        validateUniqueRegistrationAddress(newAddressRecords);
        List<Pair<Address, Boolean>> savedAddresses = saveOrLoadAddresses(newAddressRecords, false);
        createAndSetAddressRecords(newPerson, savedAddresses);

        // All associations except Address entities are saved here using cascade.
        return personRepository.save(newPerson);
    }

    // TODO add tests
    /*
     * Person update scenario:
     * We cannot save everything cascadely since some addresses may already exist, and the request will end
     * up with unique constraint violation exception. Hence, we need to process addresses separately.
     *
     * 1. Find and load the person by ID, if not exists - respond with 404.
     * 2. Extract all addresses from updatedPerson addressRecords
     *    -> list of transient Address entities
     * 3. Validate the updatedPerson has exactly one registration address
     * 4. Find ids of Addresses that need to be deleted, remember them. Deletion cannot be done right away
     *    since we still have references to them via FK from PeopleAddresses table.
     * 5. Load all existing Address entities from updatedPerson, save all new address entities.
     *    -> list of saved Address entities
     * 6. Re-create a list of PersonAddress entities from saved Address records, add them to Person ->
     *    -> updatedPerson is ready to be saved
     * 6. Save the updatedPerson Person entity. PersonAddress,Contact and IdentityDocument
     *    entities will be saved cascadely. Unique constraint violation exceptions (phone # and ID) may occur -
     *    the transaction will be rolled back.
     * 7. Since PersonAddress records with the addresses that had to be deleted has been deleted -
     *    delete all Address records the were to be deleted.
     * */
    @Override
    @Transactional
    public Person updatePerson(Person updatedPerson) {
        if (updatedPerson == null) throw new ResourceUpdateException("Updated person cannot be null");
        Long personId = updatedPerson.getId();
        if (personId == null) throw new ResourceUpdateException("Updated person must have an id");

        // loads the old addresses eagerly as well
        Person originalPerson = personRepository.findById(personId).orElseThrow(() ->
                new ResourceNotFoundException("Person with id=" + personId + " does not exist"));

        validateIdentityDocumentsForUpdate(updatedPerson.getIdentityDocuments());
        validateContactsForUpdate(updatedPerson.getContacts());

        List<PersonAddress> newAddressRecords = updatedPerson.getAddressRecords();
        validateUniqueRegistrationAddress(newAddressRecords);
        List<Pair<Address, Boolean>> updatedAddresses = saveOrLoadAddresses(newAddressRecords, true);
        createOrFindAndSetAddressRecords(updatedPerson, updatedAddresses, originalPerson.getAddressRecords());

        // All associations except Address entities are saved here using cascade.
        Person person = personRepository.save(updatedPerson);

        deleteAddressesIfUnused(originalPerson.getAddressRecords());
        return person;
    }

    private void validateUniqueRegistrationAddress(List<PersonAddress> personAddresses) {
        long count = Optional.ofNullable(personAddresses).orElse(Collections.emptyList()).stream()
                .filter(PersonAddress::getIsRegistration)
                .count();

        if (count > 1) {
            throw new ResourceConflictException("Person cannot have multiple registration addresses");
        }
    }

    private void validateIdentityDocumentsForCreation(List<IdentityDocument> identityDocuments) {
        validateExactlyOnePrimaryIdentityDocument(identityDocuments);

        for (IdentityDocument id : identityDocuments) {
            if (identityDocumentRepository.existsByTypeAndFullNumber(id.getType(), id.getFullNumber())) {
                throw new ResourceCreationException("Person's identity document with type=" + id.getType()
                        + " and fullNumber=" + id.getFullNumber() + " was requested for creation but already exists");
            }
        }
    }

    private void validateIdentityDocumentsForUpdate(List<IdentityDocument> identityDocuments) {
        validateExactlyOnePrimaryIdentityDocument(identityDocuments);

        for (IdentityDocument id : identityDocuments) {
            Long identityDocumentId = id.getId();
            if (identityDocumentId != null) {
                if (!identityDocumentRepository.existsById(identityDocumentId)) {
                    throw new ResourceNotFoundException("Person's identity document with id=" + identityDocumentId
                            + " was requested for update but does not exist");
                }
            } else if (identityDocumentRepository.existsByTypeAndFullNumber(id.getType(), id.getFullNumber())) {
                throw new ResourceCreationException("Person's identity document with type=" + id.getType()
                        + " and fullNumber=" + id.getFullNumber() + " was requested for creation but already exists");
            }
        }
    }

    private void validateExactlyOnePrimaryIdentityDocument(List<IdentityDocument> identityDocuments) {
        long count = Optional.ofNullable(identityDocuments).orElse(Collections.emptyList()).stream()
                .filter(IdentityDocument::getIsPrimary)
                .count();

        if (count != 1) {
            throw new ResourceConflictException("Person must have exactly one registration address");
        }
    }

    private void validateContactsForCreation(List<Contact> contacts) {
        if (contacts == null) return;
        List<String> phoneNumbers = contacts.stream()
                .map(Contact::getPhoneNumber)
                .collect(Collectors.toList());

        List<Contact> existingPhoneNumbers = contactRepository.findAllByPhoneNumberIn(phoneNumbers);
        if (!existingPhoneNumbers.isEmpty()) {
            throw new ResourceCreationException("Person's contact with phoneNumber=" + existingPhoneNumbers.get(0).getPhoneNumber()
                    + " was requested for creation but already exists");
        }
    }

    private void validateContactsForUpdate(List<Contact> contacts) {
        if (contacts == null) return;
        for (Contact contact : contacts) {
            Long contactId = contact.getId();
            String phoneNumber = contact.getPhoneNumber();
            if (contactId != null) {
                if (!contactRepository.existsById(contactId)) {
                    throw new ResourceNotFoundException("Person's contact with id=" + contactId +
                            " was requested for update but does not exists");
                }
            } else if (contactRepository.existsByPhoneNumber(phoneNumber)) {
                throw new ResourceCreationException("Person's contact with phoneNumber=" + phoneNumber
                        + " was requested for creation but already exists");
            }
        }
    }

    private List<Pair<Address, Boolean>> saveOrLoadAddresses(List<PersonAddress> addressRecords, boolean allowUpdate) {
        List<Pair<Address, Boolean>> transientAddressPairs = extractAddressesFromAddressRecords(addressRecords);

        List<Pair<Address, Boolean>> savedAddresses = new ArrayList<>();
        for (Pair<Address, Boolean> transientAddressPair : transientAddressPairs) {
            Address savedAddress = saveOrLoadAddress(transientAddressPair.getFirst(), allowUpdate);
            savedAddresses.add(Pair.of(savedAddress, transientAddressPair.getSecond()));
        }

        return savedAddresses;
    }

    private List<Pair<Address, Boolean>> extractAddressesFromAddressRecords(List<PersonAddress> addressRecords) {
        return Optional.ofNullable(addressRecords)
                .orElse(new ArrayList<>()).stream()
                .map(e -> Pair.of(e.getAddress(), e.getIsRegistration()))
                .collect(Collectors.toList());
    }

    private Address saveOrLoadAddress(Address transientAddress, boolean allowUpdate) {
        if (transientAddress == null) throw new ResourceConflictException("Person address must not be null");
        if (transientAddress.getRegion() == null ||
                transientAddress.getRegion().getName() == null ||
                transientAddress.getAddress() == null) {
            throw new ResourceConflictException("Person address must contain region and address");
        }

        // transientAddress may have an id
        Long addressId = transientAddress.getId();
        if (addressId != null) {
            Optional<Address> foundAddress = addressRepository.findById(addressId);
            if (foundAddress.isEmpty()) {
                throw new ResourceNotFoundException("Person's address with id=" + addressId
                        + " was requested for association but does not exist");
            } else {
                if (allowUpdate) {
                    return addressRepository.save(transientAddress);
                } else {
                    Address address = foundAddress.get();
                    if (!address.getAddress().equals(transientAddress.getAddress()) &&
                            !address.getRegion().getName().equals(transientAddress.getRegion().getName())) {
                        throw new ResourceConflictException("Person's address with id=" + addressId
                                + " was requested for association but the content varies from the content in DB");
                    }
                }
            }
        }

        return addressRepository.findByRegionNameAndAddress(
                transientAddress.getRegion().getName(),
                transientAddress.getAddress()
        ).orElseGet(() -> addressRepository.save(transientAddress));
    }

    private void createAndSetAddressRecords(Person newPerson,
                                            List<Pair<Address, Boolean>> newAddresses) {
        List<PersonAddress> updatedPersonAddresses = newAddresses.stream()
                .map(pair -> PersonAddress.builder()
                        .person(newPerson)
                        .address(pair.getFirst())
                        .isRegistration(pair.getSecond())
                        .build())
                .collect(Collectors.toList());

        newPerson.setAddressRecords(updatedPersonAddresses);
    }

    private void createOrFindAndSetAddressRecords(Person updatedPerson,
                                                  List<Pair<Address, Boolean>> updatedAddresses,
                                                  List<PersonAddress> originalAddressRecords) {
        List<PersonAddress> updatedPersonAddresses = new ArrayList<>();
        for (Pair<Address, Boolean> updatedAddress : updatedAddresses) {
            PersonAddress matchingOriginalRecord = Optional.ofNullable(originalAddressRecords)
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(ar -> ar.getAddress().getId().equals(updatedAddress.getFirst().getId()))
                    .findFirst().orElse(null);
            if (matchingOriginalRecord != null) {
                updatedPersonAddresses.add(matchingOriginalRecord);
            } else {
                PersonAddress newRecord = PersonAddress.builder()
                        .person(updatedPerson)
                        .address(updatedAddress.getFirst())
                        .isRegistration(updatedAddress.getSecond())
                        .build();
                updatedPersonAddresses.add(newRecord);
            }
        }

        updatedPerson.setAddressRecords(updatedPersonAddresses);
    }

    private void deleteAddressesIfUnused(List<PersonAddress> addressRecords) {
        List<Long> addressIds = Optional.ofNullable(addressRecords).orElse(new ArrayList<>()).stream()
                .map(PersonAddress::getAddress)
                .map(Address::getId)
                .toList();

        addressRepository.deleteAddressesByIdInAndPersonRecordsIsEmpty(addressIds);
    }

    /*
     * Method getPerson fetches the person with all associations in three steps
     * in order to avoid MultipleBagFetchException. All entity fields merges
     * happen under the hood in the persistence context.
     */
    @Override
    @Transactional(readOnly = true)
    public Person getPerson(final long personId) {
        Optional<Person> foundPerson = personRepository.findById(personId)
                .flatMap(op -> personRepository.findByIdFetchContacts(personId))
                .flatMap(op -> personRepository.findByIdFetchIdentityDocuments(personId));

        return foundPerson.orElseThrow(() ->
                new ResourceNotFoundException("Person with id=" + personId + " does not exist"));
    }

    /*
     * Methods getPeople(String, int, int) and getPeople(int, int) fetch the people with
     * all associations in four steps in order to avoid pagination in memory and cross joins.
     * All entities fields merges happen under the hood in the persistence context.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Person> getPeople(String regionName, int pageNumber, int pageSize) {
        List<Long> pagePeopleIds = personRepository.findAllIdsByRegistrationRegion(regionName,
                Pageable.ofSize(pageSize).withPage(pageNumber));

        return fetchPeopleByIds(pagePeopleIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Person> getPeople(int pageNumber, int pageSize) {
        List<Long> pagePeopleIds = personRepository.findAllIds(
                Pageable.ofSize(pageSize).withPage(pageNumber));

        return fetchPeopleByIds(pagePeopleIds);
    }

    private List<Person> fetchPeopleByIds(List<Long> peopleIds) {
        if (peopleIds.isEmpty()) return Collections.emptyList();

        // all associations are merged in persistence context with three queries
        List<Person> peoplePage = personRepository.findAllByIdsFetchAddressRecords(peopleIds);
        personRepository.findAllByIdsFetchContacts(peopleIds);
        personRepository.findAllByIdsFetchIdentityDocuments(peopleIds);

        return peoplePage;
    }

    // TODO add tests
    @Override
    @Transactional(readOnly = true)
    public boolean verifyPassport(String fullName, String passportNumber) {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }
}
