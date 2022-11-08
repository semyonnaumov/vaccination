package com.naumov.identityservice.repository;

import com.naumov.identityservice.model.IdentityDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface IdentityDocumentRepository extends JpaRepository<IdentityDocument, Long> {
    boolean existsByTypeAndFullNumber(IdentityDocument.DocumentType type, String fullNumber);
}
