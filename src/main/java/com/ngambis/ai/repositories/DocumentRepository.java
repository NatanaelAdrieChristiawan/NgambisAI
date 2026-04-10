package com.ngambis.ai.repositories;

import com.ngambis.ai.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Document} entities.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByUserId(UUID userId);

    List<Document> findByUserIdOrderByUploadedAtDesc(UUID userId);
}
