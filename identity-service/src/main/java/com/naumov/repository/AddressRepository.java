package com.naumov.repository;

import com.naumov.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("SELECT a FROM Address a WHERE a.region.name = :regionName AND a.address = :address")
    Address findByRegionNameAndAddress(String regionName, String address);
}
