package com.ngambis.ai.services;

import com.ngambis.ai.dtos.request.QuizSessionRequest;
import com.ngambis.ai.dtos.response.QuizItemResponse;
import com.ngambis.ai.dtos.response.QuizSessionResponse;
import com.ngambis.ai.exceptions.ResourceNotFoundException;
import com.ngambis.ai.models.*;
import com.ngambis.ai.repositories.DocumentRepository;
import com.ngambis.ai.repositories.QuizItemRepository;
import com.ngambis.ai.repositories.QuizSessionRepository;
import com.ngambis.ai.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling quiz session creation and management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuizSessionService {

    private final QuizSessionRepository quizSessionRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final QuizItemRepository quizItemRepository;

    /**
     * Creates a new quiz session and generates sample quiz items from the document.
     */
    @Transactional
    public QuizSessionResponse createSession(QuizSessionRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", request.getDocumentId()));

        log.info("Creating quiz session for user '{}', document '{}', persona: {}",
                user.getUsername(), document.getFilename(), request.getPersonaType());

        QuizSession session = QuizSession.builder()
                .user(user)
                .document(document)
                .personaType(request.getPersonaType())
                .build();

        QuizSession saved = quizSessionRepository.save(session);

        // Create a sample essay quiz item using the document's extracted text
        EssayItem essayItem = new EssayItem();
        essayItem.setSession(saved);
        essayItem.setQuestionText("Berdasarkan materi yang ada di dokumen, jelaskan konsep utama yang dibahas.");
        essayItem.setReferenceText(document.getExtractedText());
        quizItemRepository.save(essayItem);

        log.info("Quiz session created with ID: {}, 1 essay item generated", saved.getId());

        return getSessionById(saved.getId());
    }

    /**
     * Retrieves a quiz session by ID with its quiz items.
     */
    @Transactional(readOnly = true)
    public QuizSessionResponse getSessionById(UUID sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSession", "id", sessionId));

        List<QuizItem> items = quizItemRepository.findBySessionIdOrderByIdAsc(sessionId);

        return toResponse(session, items);
    }

    /**
     * Retrieves all sessions for a user.
     */
    @Transactional(readOnly = true)
    public List<QuizSessionResponse> getSessionsByUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        return quizSessionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(session -> {
                    List<QuizItem> items = quizItemRepository.findBySessionIdOrderByIdAsc(session.getId());
                    return toResponse(session, items);
                })
                .collect(Collectors.toList());
    }

    private QuizSessionResponse toResponse(QuizSession session, List<QuizItem> items) {
        List<QuizItemResponse> itemResponses = items.stream()
                .map(this::toQuizItemResponse)
                .collect(Collectors.toList());

        return QuizSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUser().getId())
                .documentId(session.getDocument().getId())
                .documentFilename(session.getDocument().getFilename())
                .personaType(session.getPersonaType())
                .createdAt(session.getCreatedAt())
                .quizItems(itemResponses)
                .build();
    }

    private QuizItemResponse toQuizItemResponse(QuizItem item) {
        QuizItemResponse.QuizItemResponseBuilder builder = QuizItemResponse.builder()
                .id(item.getId())
                .questionText(item.getQuestionText())
                .referenceText(item.getReferenceText() != null && item.getReferenceText().length() > 500
                        ? item.getReferenceText().substring(0, 500) + "..."
                        : item.getReferenceText());

        if (item instanceof MultipleChoiceItem mcItem) {
            builder.itemType("MULTIPLE_CHOICE")
                    .options(mcItem.getOptions())
                    .correctAnswer(mcItem.getCorrectAnswer());
        } else if (item instanceof EssayItem) {
            builder.itemType("ESSAY");
        }

        return builder.build();
    }
}
