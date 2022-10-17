package com.naumov.repository;

import com.naumov.model.PersonAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonAddressRepository extends JpaRepository<PersonAddress, Long> {
}
