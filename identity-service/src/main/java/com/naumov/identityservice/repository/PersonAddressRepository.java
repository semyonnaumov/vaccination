package com.naumov.identityservice.repository;

import com.naumov.identityservice.model.PersonAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface PersonAddressRepository extends JpaRepository<PersonAddress, Long> {
    Optional<PersonAddress> findByPersonIdAndAddressId(long personId, long addressId);
}
