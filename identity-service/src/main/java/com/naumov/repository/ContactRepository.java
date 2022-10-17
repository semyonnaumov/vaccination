package com.naumov.repository;

import com.naumov.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findAllByPhoneNumberIn(List<String> phoneNumbers);
}
