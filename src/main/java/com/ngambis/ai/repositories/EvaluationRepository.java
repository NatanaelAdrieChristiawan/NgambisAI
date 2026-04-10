package com.ngambis.ai.repositories;

import com.ngambis.ai.models.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Evaluation} entities.
 *
 * <p><b>Phase 6:</b> Added sliding window queries to fetch only the last N
 * evaluations, preventing unbounded chat history growth.</p>
 */
@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {

    List<Evaluation> findByQuizItemId(UUID quizItemId);

    List<Evaluation> findByQuizItemSessionId(UUID sessionId);

    /**
     * Sliding Window: Fetches only the LAST 3 evaluations for a given quiz item,
     * ordered by newest first. Used to limit chat history sent to the LLM.
     */
    List<Evaluation> findTop3ByQuizItemIdOrderByCreatedAtDesc(UUID quizItemId);

    /**
     * Sliding Window: Fetches only the LAST 3 evaluations for a given session,
     * ordered by newest first.
     */
    List<Evaluation> findTop3ByQuizItemSessionIdOrderByCreatedAtDesc(UUID sessionId);
}
