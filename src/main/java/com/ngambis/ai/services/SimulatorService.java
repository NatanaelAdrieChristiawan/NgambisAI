package com.ngambis.ai.services;

import com.ngambis.ai.dtos.response.AiEvaluationResponse;
import com.ngambis.ai.dtos.response.EvaluationResponse;
import com.ngambis.ai.exceptions.ResourceNotFoundException;
import com.ngambis.ai.models.*;
import com.ngambis.ai.repositories.EvaluationRepository;
import com.ngambis.ai.repositories.QuizItemRepository;
import com.ngambis.ai.repositories.QuizSessionRepository;
import com.ngambis.ai.strategies.FriendlySeniorStrategy;
import com.ngambis.ai.strategies.PersonaStrategy;
import com.ngambis.ai.strategies.StrictLecturerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Service orchestrating the oral exam simulation flow.
 *
 * <p><b>Optimizations Implemented:</b></p>
 * <ul>
 *   <li>Context Truncation: Document text reduced to relevant 2000-char window</li>
 *   <li>Sliding Window: Only last 3 evaluations included as chat history</li>
 *   <li>Rate Limiting: Prevent AI API abuse (10 calls/min per user)</li>
 *   <li>Caching: Evaluation history cached for performance</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimulatorService {

    private final AiIntegrationService aiIntegrationService;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizItemRepository quizItemRepository;
    private final EvaluationRepository evaluationRepository;
    private final StrictLecturerStrategy strictLecturerStrategy;
    private final FriendlySeniorStrategy friendlySeniorStrategy;
    private final RateLimitingService rateLimitingService;

    /**
     * Processes student's audio answer for a quiz item.
     * 
     * Flow:
     * 1. Fetch session and quiz item
     * 2. Check rate limit
     * 3. Transcribe audio
     * 4. Truncate context to relevant window
     * 5. Select persona strategy
     * 6. Fetch sliding window history (last 3)
     * 7. Generate prompt and call AI
     * 8. Save and return evaluation
     *
     * @param sessionId  quiz session ID
     * @param quizItemId quiz item being answered
     * @param audioFile  student's audio recording
     * @return AI evaluation result
     */
    @Transactional
    public EvaluationResponse evaluateAnswer(UUID sessionId, UUID quizItemId, MultipartFile audioFile) {
        // 1. Fetch session and quiz item
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSession", "id", sessionId));

        QuizItem quizItem = quizItemRepository.findById(quizItemId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizItem", "id", quizItemId));

        log.info("Evaluating answer for session '{}', item '{}', persona: {}",
                sessionId, quizItemId, session.getPersonaType());

        // 2. Check rate limit (10 calls/min per user)
        String userId = session.getUser().getId().toString();
        if (!rateLimitingService.tryConsumeToken(userId)) {
            throw new IllegalStateException("Rate limit exceeded. Please try again later.");
        }

        // 3. Transcribe audio
        String transcript = aiIntegrationService.transcribeAudio(audioFile);

        // 4. Context Truncation: Extract relevant 2000-char window
        String relevantContext = aiIntegrationService.extractRelevantContext(
                quizItem.getReferenceText(), transcript);

        log.debug("Context truncation: {} → {} chars",
                quizItem.getReferenceText() != null ? quizItem.getReferenceText().length() : 0,
                relevantContext.length());

        // 5. Select persona strategy
        PersonaStrategy strategy = resolveStrategy(session.getPersonaType());

        // 6. Generate prompt with truncated context
        String prompt = strategy.generateEvaluationPrompt(
                quizItem.getQuestionText(),
                relevantContext,
                transcript
        );

        // 7. Fetch sliding window history (last 3 evaluations)
        List<Evaluation> recentHistory = fetchSlidingWindowHistory(quizItemId);
        log.debug("Sliding window: {} recent evaluations", recentHistory.size());

        // 8. Call AI with optimized prompt + history
        AiEvaluationResponse aiResponse = aiIntegrationService.evaluateAnswer(prompt, recentHistory);

        // 9. Save evaluation with user reference
        Evaluation evaluation = Evaluation.builder()
                .quizItem(quizItem)
                .user(session.getUser())
                .studentAudioTranscript(transcript)
                .score(aiResponse.getScore())
                .feedback(aiResponse.getFeedback())
                .build();

        Evaluation saved = evaluationRepository.save(evaluation);
        log.info("Evaluation saved — ID: {}, Score: {}", saved.getId(), saved.getScore());

        return toResponse(saved, quizItem);
    }

    /**
     * Processes student's text answer for a quiz item.
     * Used when the frontend transcribes audio via Web Speech API (FREE, no billing).
     *
     * Flow:
     * 1. Fetch session and quiz item
     * 2. Check rate limit
     * 3. Validate browser transcript
     * 4. Truncate context to relevant window
     * 5. Select persona strategy
     * 6. Fetch sliding window history (last 3)
     * 7. Generate prompt and call AI
     * 8. Save and return evaluation
     *
     * @param sessionId  quiz session ID
     * @param quizItemId quiz item being answered
     * @param transcript student's spoken answer (transcribed by browser Web Speech API)
     * @return AI evaluation result
     */
    @Transactional
    public EvaluationResponse evaluateTextAnswer(UUID sessionId, UUID quizItemId, String transcript) {
        // 1. Fetch session and quiz item
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSession", "id", sessionId));

        QuizItem quizItem = quizItemRepository.findById(quizItemId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizItem", "id", quizItemId));

        log.info("Evaluating text answer for session '{}', item '{}', persona: {}",
                sessionId, quizItemId, session.getPersonaType());

        // 2. Check rate limit (10 calls/min per user)
        String userId = session.getUser().getId().toString();
        if (!rateLimitingService.tryConsumeToken(userId)) {
            throw new IllegalStateException("Rate limit exceeded. Please try again later.");
        }

        // 3. Validate browser transcript (from Web Speech API)
        String validatedTranscript = aiIntegrationService.validateBrowserTranscript(transcript);

        // 4. Context Truncation: Extract relevant 2000-char window
        String relevantContext = aiIntegrationService.extractRelevantContext(
                quizItem.getReferenceText(), validatedTranscript);

        log.debug("Context truncation: {} → {} chars",
                quizItem.getReferenceText() != null ? quizItem.getReferenceText().length() : 0,
                relevantContext.length());

        // 5. Select persona strategy
        PersonaStrategy strategy = resolveStrategy(session.getPersonaType());

        // 6. Generate prompt with truncated context
        String prompt = strategy.generateEvaluationPrompt(
                quizItem.getQuestionText(),
                relevantContext,
                validatedTranscript
        );

        // 7. Fetch sliding window history (last 3 evaluations)
        List<Evaluation> recentHistory = fetchSlidingWindowHistory(quizItemId);
        log.debug("Sliding window: {} recent evaluations", recentHistory.size());

        // 8. Call AI with optimized prompt + history
        AiEvaluationResponse aiResponse = aiIntegrationService.evaluateAnswer(prompt, recentHistory);

        // 9. Save evaluation with user reference
        Evaluation evaluation = Evaluation.builder()
                .quizItem(quizItem)
                .user(session.getUser())
                .studentAudioTranscript(validatedTranscript)
                .score(aiResponse.getScore())
                .feedback(aiResponse.getFeedback())
                .build();

        Evaluation saved = evaluationRepository.save(evaluation);
        log.info("Evaluation saved — ID: {}, Score: {}", saved.getId(), saved.getScore());

        return toResponse(saved, quizItem);
    }

    /**
     * Fetches last 3 evaluations for a quiz item (sliding window).
     * Results are reversed to chronological order for AI context.
     *
     * @param quizItemId quiz item to fetch history for
     * @return last 3 evaluations in chronological order
     */
    @Cacheable(value = "evaluations", key = "#quizItemId + '-history'")
    private List<Evaluation> fetchSlidingWindowHistory(UUID quizItemId) {
        List<Evaluation> history = evaluationRepository
                .findTop3ByQuizItemIdOrderByCreatedAtDesc(quizItemId);

        if (history.isEmpty()) {
            return Collections.emptyList();
        }

        // Reverse to chronological order for natural conversation flow
        List<Evaluation> chronological = new ArrayList<>(history);
        Collections.reverse(chronological);

        return chronological;
    }

    /**
     * Resolves PersonaStrategy based on persona type.
     * Core of Strategy Pattern — runtime strategy selection.
     */
    private PersonaStrategy resolveStrategy(PersonaType personaType) {
        return switch (personaType) {
            case STRICT_LECTURER -> strictLecturerStrategy;
            case FRIENDLY_SENIOR -> friendlySeniorStrategy;
        };
    }

    /**
     * Maps Evaluation entity to EvaluationResponse DTO.
     */
    private EvaluationResponse toResponse(Evaluation evaluation, QuizItem quizItem) {
        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .quizItemId(quizItem.getId())
                .questionText(quizItem.getQuestionText())
                .studentAudioTranscript(evaluation.getStudentAudioTranscript())
                .score(evaluation.getScore())
                .feedback(evaluation.getFeedback())
                .createdAt(evaluation.getCreatedAt())
                .build();
    }
}
