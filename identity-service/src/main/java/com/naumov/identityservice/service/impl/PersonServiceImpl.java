package com.naumov.identityservice.service.impl;

import com.naumov.identityservice.exception.ResourceConflictException;
import com.naumov.identityservice.exception.ResourceCreationException;
import com.naumov.identityservice.exception.ResourceNotFoundException;
import com.naumov.identityservice.exception.ResourceUpdateException;
import com.naumov.identityservice.model.*;
import com.naumov.identityservice.repository.*;
import com.naumov.identityservice.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PersonServiceImpl implements PersonService {
    private final PersonRepository personRepository;
    private final AddressRepository addressRepository;
    private final PersonAddressRepository personAddressRepository;
    private final ContactRepository contactRepository;
    private final IdentityDocumentRepository identityDocumentRepository;
    private final RegionRepository regionRepository;

    @Autowired
    public PersonServiceImpl(PersonRepository personRepository,
                             AddressRepository addressRepository,
                             PersonAddressRepository personAddressRepository,
                             ContactRepository contactRepository,
                             IdentityDocumentRepository identityDocumentRepository,
                             RegionRepository regionRepository) {
        this.personRepository = personRepository;
        this.addressRepository = addressRepository;
        this.personAddressRepository = personAddressRepository;
        this.contactRepository = contactRepository;
        this.identityDocumentRepository = identityDocumentRepository;
        this.regionRepository = regionRepository;
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
        if (newPerson.getId() != null) throw new ResourceCreationException("Created person cannot have an ID");

        validateIdentityDocuments(newPerson.getIdentityDocuments(), false);
        validateContacts(newPerson.getContacts(), false);
        validateAddressRecords(newPerson.getAddressRecords(), false);
        saveOrLoadAddresses(newPerson, false);

        // All associations except Address entities are saved here using cascade.
        return personRepository.save(newPerson);
    }

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

        Set<Long> detachedAddressesIds = findDetachedAddressesIds(updatedPerson, originalPerson);

        validateIdentityDocuments(updatedPerson.getIdentityDocuments(), true);
        validateContacts(updatedPerson.getContacts(), true);
        validateAddressRecords(updatedPerson.getAddressRecords(), true);
        saveOrLoadAddresses(updatedPerson, true);

        // All associations except Address entities are saved here using cascade.
        Person person = personRepository.save(updatedPerson);

        deleteAddressesByIdsIfUnused(detachedAddressesIds);
        return person;
    }

    private Set<Long> findDetachedAddressesIds(Person updatedPerson, Person originalPerson) {
        Set<Long> newIds = extractAddressesIds(updatedPerson);
        Set<Long> oldIds = extractAddressesIds(originalPerson);
        oldIds.removeAll(newIds);
        return oldIds;
    }

    private Set<Long> extractAddressesIds(Person person) {
        if (person == null || person.getAddressRecords() == null) return Collections.emptySet();

        return person.getAddressRecords().stream()
                .map(PersonAddress::getAddress)
                .filter(Objects::nonNull)
                .map(Address::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void validateIdentityDocuments(List<IdentityDocument> identityDocuments, boolean allowUpdate) {
        if (identityDocuments == null) throw new ResourceConflictException("Person's identityDocuments cannot be null");

        if (!allowUpdate) {
            boolean anyIdExists = identityDocuments.stream().anyMatch(doc -> doc.getId() != null);
            if (anyIdExists)
                throw new ResourceCreationException("None of person's identityDocuments should nave an ID " +
                        "during person creation");
        }

        validateExactlyOnePrimaryIdentityDocument(identityDocuments);

        for (IdentityDocument id : identityDocuments) {
            Long identityDocumentId = id.getId();
            if (identityDocumentId != null) {
                if (!allowUpdate) throw new ResourceCreationException("Person's identity document with type=" +
                        id.getType() + " and fullNumber=" + id.getFullNumber() +
                        " was requested for creation but contains non-null id (id=" + identityDocumentId + ")");

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
        long count = identityDocuments.stream()
                .filter(IdentityDocument::getIsPrimary)
                .count();

        if (count != 1) {
            throw new ResourceConflictException("Person must have exactly one registration address");
        }
    }

    private void validateContacts(List<Contact> contacts, boolean allowUpdate) {
        if (contacts == null) throw new ResourceConflictException("Person's contacts cannot be null");

        if (!allowUpdate) {
            boolean anyIdExists = contacts.stream().anyMatch(contact -> contact.getId() != null);
            if (anyIdExists) throw new ResourceCreationException("None of person's contacts should nave an ID " +
                    "during person creation");
        }

        for (Contact contact : contacts) {
            Long contactId = contact.getId();
            String phoneNumber = contact.getPhoneNumber();
            if (contactId != null) {
                if (!allowUpdate) throw new ResourceCreationException("Person's contact with phoneNumber=" +
                        phoneNumber + " was requested for creation but contains non-null id (id=" + contact + ")");

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

    private void validateAddressRecords(List<PersonAddress> addressRecords, boolean allowUpdate) {
        if (addressRecords == null) throw new ResourceConflictException("Person's addressRecords cannot be null");

        if (!allowUpdate) {
            boolean anyAddressRecordIdExists = addressRecords.stream().anyMatch(contact -> contact.getId() != null);
            if (anyAddressRecordIdExists) throw new ResourceCreationException("None of person's addressRecords " +
                    "should nave an ID during person creation");

            boolean anyAddressIdExists = addressRecords.stream()
                    .map(PersonAddress::getAddress)
                    .anyMatch(contact -> contact.getId() != null);
            if (anyAddressIdExists) throw new ResourceCreationException("None of person's addresses " +
                    "should nave an ID during person creation");
        }

        long count = addressRecords.stream()
                .filter(PersonAddress::getIsRegistration)
                .count();

        if (count > 1) {
            throw new ResourceConflictException("Person cannot have multiple registration addresses");
        }
    }

    private void saveOrLoadAddresses(Person person, boolean allowUpdate) {
        List<PersonAddress> addressRecords = person.getAddressRecords();
        if (addressRecords == null) return;

        List<PersonAddress> newAddressRecords = new ArrayList<>();
        for (PersonAddress addressRecord : addressRecords) {
            Address savedAddress = saveOrLoadAddress(addressRecord.getAddress(), allowUpdate);
            PersonAddress newAddressRecord = PersonAddress.builder()
                    .id(addressRecord.getId())
                    .person(person)
                    .address(savedAddress)
                    .isRegistration(addressRecord.getIsRegistration())
                    .build();

            if (allowUpdate) {
                Optional<PersonAddress> savedAddressRecord = findRelatedSavedAddressRecord(person.getId(), savedAddress.getId());
                if (savedAddressRecord.isPresent()) newAddressRecord = savedAddressRecord.get();
            }

            newAddressRecords.add(newAddressRecord);
        }

        person.setAddressRecords(newAddressRecords);
    }

    private Address saveOrLoadAddress(Address transientAddress, boolean allowUpdate) {
        if (transientAddress == null)
            throw new ResourceConflictException("Person address must not be null");
        if (transientAddress.getAddress() == null)
            throw new ResourceConflictException("Person address must contain address");

        loadAndSetRegion(transientAddress);

        Long addressId = transientAddress.getId();
        if (addressId != null) {
            if (!allowUpdate) throw new ResourceCreationException("Person's address was requested for creation " +
                    "but contains non-null id (id=" + addressId + ")");

            Optional<Address> foundAddress = addressRepository.findById(addressId);
            if (foundAddress.isEmpty()) {
                throw new ResourceNotFoundException("Person's address with id=" + addressId
                        + " was requested for association but does not exist");
            } else {
                return addressRepository.save(transientAddress);
            }
        }

        return addressRepository.findByRegionNameAndAddress(
                transientAddress.getRegion().getName(),
                transientAddress.getAddress()
        ).orElseGet(() -> addressRepository.save(transientAddress));
    }

    private void loadAndSetRegion(Address transientAddress) {
        if (transientAddress.getRegion() == null || transientAddress.getRegion().getName() == null)
            throw new ResourceConflictException("Person address must contain region with name");

        String regionName = transientAddress.getRegion().getName();
        Region region = regionRepository.findByName(regionName).orElseThrow(() ->
                new ResourceConflictException("Person's address contains non-existing region with name=" + regionName));
        transientAddress.setRegion(region);
    }

    private Optional<PersonAddress> findRelatedSavedAddressRecord(Long personId, Long addressId) {
        Objects.requireNonNull(personId, "Person id must not be null for PersonAddress search");
        Objects.requireNonNull(addressId, "Address id must not be null for PersonAddress search");

        return personAddressRepository.findByPersonIdAndAddressId(personId, addressId);
    }

    private void deleteAddressesByIdsIfUnused(Set<Long> addressesIds) {
        addressRepository.deleteAddressesByIdInAndPersonRecordsIsEmpty(addressesIds);
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

    @Override
    @Transactional(readOnly = true)
    public boolean verifyPassport(String fullName, String passportNumber) {
        return personRepository.verifyPassport(fullName, passportNumber);
    }
}
