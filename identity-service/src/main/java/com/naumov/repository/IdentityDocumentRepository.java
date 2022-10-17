package com.naumov.repository;

import com.naumov.model.IdentityDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface IdentityDocumentRepository extends JpaRepository<IdentityDocument, Long> {
    Optional<IdentityDocument> findByTypeAndFullNumber(IdentityDocument.DocumentType type, String fullNumber);
}
