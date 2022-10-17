package com.naumov.repository;

import com.naumov.model.Person;
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
    @Query("""
            FROM Person p
            LEFT OUTER JOIN FETCH p.contacts
            WHERE p.id = :id
            """)
    Optional<Person> findByIdFetchContacts(Long id);

    @Query("""
            FROM Person p
            LEFT OUTER JOIN FETCH p.identityDocuments
            WHERE p.id = :id
            """)
    Optional<Person> findByIdFetchIdentityDocuments(Long id);

    // HHH000104 solution - 2 methods: findAllIdsByRegistrationRegion and findAllByIds
    @Query("SELECT p.id FROM Person p " +
            "JOIN p.addressRecords ar " +
            "JOIN ar.address a " +
            "JOIN a.region r " +
            "WHERE ar.isRegistration = true AND r.name = :regionName " +
            "ORDER BY p.id")
    List<Long> findAllIdsByRegistrationRegion(String regionName, Pageable pageable);

    // TODO will be subjected to MultipleBagFetchException, redo
    @Query("FROM Person p " +
            "JOIN FETCH PersonAddress pa " +
            "JOIN FETCH Address a " +
            "JOIN FETCH Region r " +
            "JOIN FETCH Contact c " +
            "JOIN FETCH IdentityDocument id " +
            "WHERE p.id IN :ids " +
            "ORDER BY p.id")
    List<Person> findAllByIds(List<Long> ids);
}
