package com.naumov.identityservice.repository;

import com.naumov.identityservice.model.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface PersonRepository extends JpaRepository<Person, Long> {

    // Two queries to sequentially load a Person object avoiding MultipleBagFetchException
    @Query("FROM Person p " +
            "LEFT OUTER JOIN FETCH p.contacts " +
            "WHERE p.id = :id")
    Optional<Person> findByIdFetchContacts(Long id);

    @Query("FROM Person p " +
            "LEFT OUTER JOIN FETCH p.identityDocuments " +
            "WHERE p.id = :id")
    Optional<Person> findByIdFetchIdentityDocuments(Long id);

    // Pagination in memory (HHH000104) and cross join (MultipleBagFetchException) solution -
    // two stage select: findAllIdsByRegistrationRegion (findAllIds) and findAllByIdsFetch* methods
    @Query("SELECT p.id FROM Person p " +
            "JOIN p.addressRecords ar " +
            "JOIN ar.address a " +
            "JOIN a.region r " +
            "WHERE ar.isRegistration = true AND r.name = :regionName " +
            "ORDER BY p.id")
    List<Long> findAllIdsByRegistrationRegion(String regionName, Pageable pageable);

    @Query("SELECT p.id FROM Person p " +
            "ORDER BY p.id")
    List<Long> findAllIds(Pageable withPage);

    @Query("FROM Person p " +
            "LEFT JOIN FETCH p.addressRecords ar " +
            "JOIN FETCH ar.address a " +
            "JOIN FETCH a.region " +
            "WHERE p.id IN :ids " +
            "ORDER BY p.id")
    List<Person> findAllByIdsFetchAddressRecords(List<Long> ids);

    @Query("FROM Person p " +
            "LEFT JOIN FETCH p.contacts " +
            "WHERE p.id IN :ids " +
            "ORDER BY p.id")
    List<Person> findAllByIdsFetchContacts(List<Long> ids);

    @Query("FROM Person p " +
            "LEFT JOIN FETCH p.identityDocuments " +
            "WHERE p.id IN :ids " +
            "ORDER BY p.id")
    List<Person> findAllByIdsFetchIdentityDocuments(List<Long> ids);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Person p " +
            "JOIN p.identityDocuments id " +
            "WHERE p.name = :fullName " +
            "AND id.type = 'INNER_PASSPORT' " +
            "AND id.fullNumber = :passportNumber")
    boolean verifyPassport(String fullName, String passportNumber);
}
