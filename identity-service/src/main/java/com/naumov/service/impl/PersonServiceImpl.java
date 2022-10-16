package com.naumov.service.impl;

import com.naumov.model.Address;
import com.naumov.model.Person;
import com.naumov.model.PersonAddress;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
// TODO revise this service
public class PersonServiceImpl implements PersonService {
    private final PersonRepository personRepository;
    private final AddressRepository addressRepository;
    private final ContactRepository contactRepository;
    private final IdentityDocumentRepository idRepo;

    @Autowired
    public PersonServiceImpl(PersonRepository personRepository,
                             AddressRepository addressRepository,
                             ContactRepository contactRepository,
                             IdentityDocumentRepository idRepo) {
        this.personRepository = personRepository;
        this.addressRepository = addressRepository;
        this.contactRepository = contactRepository;
        this.idRepo = idRepo;
    }

    @Override
    @Transactional
    public Person createPerson(Person newPerson) {
        if (newPerson == null) throw new IllegalArgumentException("Argument newPerson cannot be null");

        fetchOrSaveAddresses(newPerson);
        // todo find out what to do with these
//        fetchOrSaveContacts(newPerson);
//        fetchOrSaveIdentityDocuments(newPerson);

        return personRepository.save(newPerson);
    }

    // newly come addresses may already exist in the DB
    // we need to find them by unique fields and fetch from DB with ids
    // and merge them with other addresses
    private void fetchOrSaveAddresses(Person person) {
        List<Pair<Address, Boolean>> addresses = extractAddresses(person);
        List<Pair<Address, Boolean>> persistedAddresses = updateRawAddresses(addresses);
        List<PersonAddress> addressRecords = createAddressRecords(person, persistedAddresses);
        person.setAddressRecords(addressRecords);
    }

    private List<Pair<Address, Boolean>> extractAddresses(Person person) {
        return Optional.of(person.getAddressRecords())
                .orElse(new ArrayList<>()).stream()
                .map(e -> Pair.of(e.getAddress(), e.getIsRegistration()))
                .collect(Collectors.toList());
    }

    private List<Pair<Address, Boolean>> updateRawAddresses(List<Pair<Address, Boolean>> rawAddresses) {
        List<Pair<Address, Boolean>> persistedAddresses = new ArrayList<>();
        for (Pair<Address, Boolean> rawAddressPair : rawAddresses) {
            Address persistedAddress = findAddress(rawAddressPair.getFirst());
            persistedAddress = persistedAddress != null ? persistedAddress : addressRepository.save(rawAddressPair.getFirst());
            persistedAddresses.add(Pair.of(persistedAddress, rawAddressPair.getSecond()));
        }

        return persistedAddresses;
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

    private List<PersonAddress> createAddressRecords(Person newPerson, List<Pair<Address, Boolean>> addresses) {
        return addresses.stream()
                .map(p -> PersonAddress.builder()
                        .person(newPerson)
                        .address(p.getFirst())
                        .isRegistration(p.getSecond())
                        .build()
                ).toList();
    }

    private Address findAddress(Address rawAddress) {
        if (rawAddress == null ||
                rawAddress.getRegion() == null ||
                rawAddress.getRegion().getName() == null ||
                rawAddress.getAddress() == null) {
            throw new IllegalStateException("Address entity is incorrect to be used in a search");
        }

        if (rawAddress.getId() != null) {
            return addressRepository.findById(rawAddress.getId()).orElseThrow(() ->
                    new IllegalStateException("Cannot find user with id=" + rawAddress.getId()));
        }

        return addressRepository.findByRegionNameAndAddress(rawAddress.getRegion().getName(), rawAddress.getAddress());
    }

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

    @Override
    @Transactional
    public Person updatePerson(Person updatedPerson) {
        Person persistedPerson = personRepository.findById(updatedPerson.getId()).orElseThrow(() ->
                new IllegalStateException("Cannot find user with id=" + updatedPerson.getId()));

        persistedPerson.setName(updatedPerson.getName());
        persistedPerson.setDateOfBirth(updatedPerson.getDateOfBirth());
        persistedPerson.setIsHidden(updatedPerson.getIsHidden());
        persistedPerson.setAddressRecords(updatedPerson.getAddressRecords());
        persistedPerson.setContacts(updatedPerson.getContacts());
        persistedPerson.setIdentityDocuments(updatedPerson.getIdentityDocuments());

        fetchOrSaveAddresses(persistedPerson);
        // todo find out what to do with these
//        fetchOrSaveContacts(persistedPerson);
//        fetchOrSaveIdentityDocuments(persistedPerson);

        return personRepository.save(persistedPerson);
    }

    @Override
    public Person getPerson(long personId) {
        return personRepository.findById(personId).orElse(null);
    }

    @Override
    public List<Person> getPeople(int pageNumber, int pageSize) {
        return personRepository.findAll(Pageable.ofSize(pageSize).withPage(pageNumber)).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Person> getPeople(String regionName, int pageNumber, int pageSize) {
        List<Integer> pagePeopleIds = personRepository.findAllIdsByRegistrationRegion(regionName,
                Pageable.ofSize(pageSize).withPage(pageNumber));

        return personRepository.findByIds(pagePeopleIds);
    }

    // todo
    @Override
    public boolean verifyPassport(String fullName, String passportNumber) {
        return false;
    }
}
