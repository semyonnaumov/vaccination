package com.naumov.repository;

import com.naumov.model.Address;
import com.naumov.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("FROM Address a " +
            "LEFT OUTER JOIN FETCH a.personRecords pa " +
            "JOIN FETCH a.region r " +
            "WHERE a.address = :address AND r.name = :regionName")
    Optional<Address> findByRegionNameAndAddress(String regionName, String address);
}
