package com.ngambis.ai.repositories;

import com.ngambis.ai.models.QuizItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link QuizItem} entities and its subclasses.
 * Works with both {@link com.ngambis.ai.models.MultipleChoiceItem}
 * and {@link com.ngambis.ai.models.EssayItem} via SINGLE_TABLE inheritance.
 */
@Repository
public interface QuizItemRepository extends JpaRepository<QuizItem, UUID> {

    List<QuizItem> findBySessionId(UUID sessionId);

    List<QuizItem> findBySessionIdOrderByIdAsc(UUID sessionId);
}
