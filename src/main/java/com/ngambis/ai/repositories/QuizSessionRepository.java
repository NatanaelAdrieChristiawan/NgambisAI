package com.ngambis.ai.repositories;

import com.ngambis.ai.models.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link QuizSession} entities.
 */
@Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, UUID> {

    List<QuizSession> findByUserId(UUID userId);

    List<QuizSession> findByDocumentId(UUID documentId);

    List<QuizSession> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<QuizSession> findByUserIdOrderByPinnedDescCreatedAtDesc(UUID userId);
}
