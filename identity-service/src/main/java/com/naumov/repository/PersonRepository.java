package com.naumov.repository;

import com.naumov.model.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    // HHH000104 solution - 2 queries
    @Query("SELECT p.id FROM Person p " +
            "JOIN PersonAddress pa " +
            "JOIN Address a " +
            "JOIN Region r " +
            "WHERE pa.isRegistration = true AND r.name = :regionName " +
            "ORDER BY p.id")
    List<Integer> findAllIdsByRegistrationRegion(String regionName, Pageable pageable);

    @Query("FROM Person p " +
            "JOIN FETCH PersonAddress pa " + // todo check Address and Region for JOIN FETCH
            "WHERE p.id IN :ids " +
            "ORDER BY p.id")
    List<Person> findByIds(List<Integer> ids);
}
