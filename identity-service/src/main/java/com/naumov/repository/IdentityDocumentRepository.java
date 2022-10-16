package com.naumov.repository;

import com.naumov.model.IdentityDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdentityDocumentRepository extends JpaRepository<IdentityDocument, Long> {
    IdentityDocument findByTypeAndFullNumber(IdentityDocument.DocumentType type, String fullNumber);
}
