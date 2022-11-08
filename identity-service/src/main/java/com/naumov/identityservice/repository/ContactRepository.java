package com.naumov.identityservice.repository;

import com.naumov.identityservice.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ContactRepository extends JpaRepository<Contact, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    List<Contact> findAllByPhoneNumberIn(List<String> phoneNumbers);
}
