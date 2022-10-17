package com.naumov.service.impl;

import com.naumov.exception.EntityNotFoundException;
import com.naumov.exception.PersonConsistencyException;
import com.naumov.model.Address;
import com.naumov.model.IdentityDocument;
import com.naumov.model.Person;
import com.naumov.model.PersonAddress;
import com.naumov.repository.AddressRepository;
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

    @Autowired
    public PersonServiceImpl(PersonRepository personRepository,
                             AddressRepository addressRepository) {
        this.personRepository = personRepository;
        this.addressRepository = addressRepository;
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
    public Person createPerson(Person transientPerson) {
        if (transientPerson == null) throw new PersonConsistencyException("Person cannot be null");

        List<Pair<Address, Boolean>> transientAddressPairs = extractAddresses(transientPerson);
        validateUniqueRegistrationAddress(transientPerson.getAddressRecords());
        validateExactlyOnePrimaryIdentityDocument(transientPerson.getIdentityDocuments());
        List<Pair<Address, Boolean>> savedAddresses = saveOrLoadAddresses(transientAddressPairs);
        updatePersonAddresses(transientPerson, savedAddresses);

        // All associations except Address entities are saved here using cascade.
        return personRepository.save(transientPerson);
    }

    private List<Pair<Address, Boolean>> extractAddresses(Person person) {
        return Optional.ofNullable(person.getAddressRecords())
                .orElse(new ArrayList<>()).stream()
                .map(e -> Pair.of(e.getAddress(), e.getIsRegistration()))
                .collect(Collectors.toList());
    }

    private void validateUniqueRegistrationAddress(List<PersonAddress> personAddresses) {
        long count = Optional.ofNullable(personAddresses).orElse(Collections.emptyList()).stream()
                .filter(PersonAddress::getIsRegistration)
                .count();

        if (count > 1) {
            throw new PersonConsistencyException("Person cannot have multiple registration addresses");
        }
    }

    private void validateExactlyOnePrimaryIdentityDocument(List<IdentityDocument> identityDocuments) {
        long count = Optional.ofNullable(identityDocuments).orElse(Collections.emptyList()).stream()
                .filter(IdentityDocument::getIsPrimary)
                .count();

        if (count != 1) {
            throw new PersonConsistencyException("Person must have exactly one registration address");
        }
    }

    private List<Pair<Address, Boolean>> saveOrLoadAddresses(List<Pair<Address, Boolean>> transientAddressPairs) {
        List<Pair<Address, Boolean>> savedAddresses = new ArrayList<>();
        for (Pair<Address, Boolean> transientAddressPair : transientAddressPairs) {
            Address savedAddress = saveOrLoadAddress(transientAddressPair.getFirst());
            savedAddresses.add(Pair.of(savedAddress, transientAddressPair.getSecond()));
        }

        return savedAddresses;
    }

    private Address saveOrLoadAddress(Address transientAddress) {
        if (transientAddress == null ||
                transientAddress.getRegion() == null ||
                transientAddress.getRegion().getName() == null ||
                transientAddress.getAddress() == null) {
            throw new PersonConsistencyException("Address is incorrect to be used in a search");
        }

        // transientAddress may have an id
        if (transientAddress.getId() != null) {
            return addressRepository.findById(transientAddress.getId()).orElseThrow(() ->
                    new PersonConsistencyException("Cannot find address with id=" + transientAddress.getId()));
        }

        return addressRepository.findByRegionNameAndAddress(
                transientAddress.getRegion().getName(),
                transientAddress.getAddress()
        ).orElseGet(() -> addressRepository.save(transientAddress));
    }

    private void updatePersonAddresses(Person transientPerson, List<Pair<Address, Boolean>> savedAddresses) {
        List<PersonAddress> updatedPersonAddresses = savedAddresses.stream()
                .map(pair -> PersonAddress.builder()
                        .person(transientPerson)
                        .address(pair.getFirst())
                        .isRegistration(pair.getSecond())
                        .build())
                .collect(Collectors.toList());

        transientPerson.setAddressRecords(updatedPersonAddresses);
    }

    // TODO add tests
    @Override
    @Transactional(readOnly = true)
    public Person getPerson(long personId) {
        // TODO use multiple methods to fetch lazy associations here
        return personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person with id=" + personId + " does not exist"));
    }

    // TODO add tests
    @Override
    @Transactional(readOnly = true)
    public List<Person> getPeople(String regionName, int pageNumber, int pageSize) {
        List<Long> pagePeopleIds = personRepository.findAllIdsByRegistrationRegion(regionName,
                Pageable.ofSize(pageSize).withPage(pageNumber));

        return personRepository.findAllByIds(pagePeopleIds);
    }

    // TODO add tests
    @Override
    @Transactional(readOnly = true)
    public List<Person> getPeople(int pageNumber, int pageSize) {
        return personRepository.findAll(Pageable.ofSize(pageSize).withPage(pageNumber)).getContent();
    }

    // TODO add tests
    @Override
    @Transactional(readOnly = true)
    public boolean verifyPassport(String fullName, String passportNumber) {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }

    // TODO add tests
    @Override
    @Transactional
    public Person updatePerson(Person updatedPerson) {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented");
//
//        Person persistedPerson = personRepository.findById(updatedPerson.getId()).orElseThrow(() ->
//                new IllegalStateException("Cannot find user with id=" + updatedPerson.getId()));
//
//        persistedPerson.setName(updatedPerson.getName());
//        persistedPerson.setDateOfBirth(updatedPerson.getDateOfBirth());
//        persistedPerson.setIsHidden(updatedPerson.getIsHidden());
//        persistedPerson.setAddressRecords(updatedPerson.getAddressRecords());
//        persistedPerson.setContacts(updatedPerson.getContacts());
//        persistedPerson.setIdentityDocuments(updatedPerson.getIdentityDocuments());
//
//        List<Pair<Address, Boolean>> savedAddresses = saveOrLoadAddresses(persistedPerson);
//        // todo find out what to do with these
////        fetchOrSaveContacts(persistedPerson);
////        fetchOrSaveIdentityDocuments(persistedPerson);
//
//        return personRepository.save(persistedPerson);
    }

    private List<PersonAddress> updateAddressRecords(Person transientPerson, List<Pair<Address, Boolean>> savedAddresses) {
        return savedAddresses.stream()
                .map(p -> PersonAddress.builder()
                        .person(transientPerson)
                        .address(p.getFirst())
                        .isRegistration(p.getSecond())
                        .build()
                ).toList();
    }

//    private <E> List<E> updateRawEntities(Collection<E> rawEntities,
//                                          JpaRepository<E, ?> repo,
//                                          Function<E, E> findMethod) {
//        List<E> persistedEntities = new ArrayList<>();
//        for (E entity : rawEntities) {
//            E persistedEntity = findMethod.apply(entity);
//            persistedEntities.add(Objects.requireNonNullElseGet(persistedEntity,
//                    () -> repo.save(entity)));
//        }
//
//        return persistedEntities;
//    }

//    private void fetchOrSaveContacts(Person newPerson) {
//        List<Contact> contacts = Optional.of(newPerson.getContacts()).orElse(new ArrayList<>());
//        List<Contact> persistedContacts = updateRawEntities(contacts, contactRepo, this::findContact);
//
//        newPerson.setContacts(persistedContacts);
//    }
//
//    private Contact findContact(Contact rawContact) {
//        if (rawContact == null || rawContact.getPhoneNumber() == null) {
//            throw new IllegalStateException("Contact entity is incorrect to be used in a search");
//        }
//
//        return contactRepo.findByPhoneNumber(rawContact.getPhoneNumber());
//    }

//    private void fetchOrSaveIdentityDocuments(Person newPerson) {
//        List<IdentityDocument> ids = Optional.of(newPerson.getIdentityDocuments()).orElse(new ArrayList<>());
//        List<IdentityDocument> persistedIds = updateRawEntities(ids, idRepo, this::findIdentityDocument);
//
//        newPerson.setIdentityDocuments(persistedIds);
//    }
//
//    private IdentityDocument findIdentityDocument(IdentityDocument rawId) {
//        if (rawId == null || rawId.getType() == null || rawId.getFullNumber() == null) {
//            throw new IllegalStateException("IdentityDocument entity is incorrect to be used in a search");
//        }
//
//        return idRepo.findByTypeAndFullNumber(rawId.getType(), rawId.getFullNumber());
//    }
}
