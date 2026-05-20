package com.ngambis.ai.services;

import com.ngambis.ai.dtos.request.QuizSessionRequest;
import com.ngambis.ai.dtos.response.GeneratedQuizItemDto;
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
    private final AiIntegrationService aiIntegrationService;

    /**
     * Creates a new quiz session and generates AI-powered quiz items from the document(s).
     * Supports multiple documents — extracted texts are merged before generation.
     */
    @Transactional
    public QuizSessionResponse createSession(QuizSessionRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        // Fetch all requested documents
        List<Document> documents = documentRepository.findAllById(request.getDocumentIds());
        if (documents.isEmpty()) {
            throw new ResourceNotFoundException("Document", "ids", request.getDocumentIds());
        }

        // Use first document as primary (for session FK); merge all texts for AI context
        Document primaryDocument = documents.get(0);

        // Merge extracted text from all documents
        String mergedText = documents.stream()
                .map(Document::getExtractedText)
                .filter(text -> text != null && !text.isBlank())
                .collect(java.util.stream.Collectors.joining("\n\n---\n\n"));

        log.info("Creating quiz session for user '{}', {} document(s), persona: {}, type: {}, count: {}",
                user.getUsername(), documents.size(), request.getPersonaType(),
                request.getItemType(), request.getQuestionCount());

        QuizSession session = QuizSession.builder()
                .user(user)
                .document(primaryDocument)
                .personaType(request.getPersonaType())
                .build();

        QuizSession saved = quizSessionRepository.save(session);

        // Generate AI questions
        String itemType = request.getItemType() != null ? request.getItemType().toUpperCase() : "ESSAY";
        int count = request.getQuestionCount() > 0 ? request.getQuestionCount() : 5;

        List<GeneratedQuizItemDto> generatedItems = aiIntegrationService.generateQuizItems(
                mergedText, count, itemType);

        // Persist generated items
        for (GeneratedQuizItemDto dto : generatedItems) {
            if ("MULTIPLE_CHOICE".equals(dto.getItemType())) {
                MultipleChoiceItem mcItem = new MultipleChoiceItem();
                mcItem.setSession(saved);
                mcItem.setQuestionText(dto.getQuestionText());
                mcItem.setReferenceText(dto.getReferenceText());
                mcItem.setOptions(dto.getOptions());
                mcItem.setCorrectAnswer(dto.getCorrectAnswer());
                quizItemRepository.save(mcItem);
            } else {
                EssayItem essayItem = new EssayItem();
                essayItem.setSession(saved);
                essayItem.setQuestionText(dto.getQuestionText());
                essayItem.setReferenceText(dto.getReferenceText());
                quizItemRepository.save(essayItem);
            }
        }

        log.info("Quiz session created — ID: {}, {} items generated", saved.getId(), generatedItems.size());

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

        return quizSessionRepository.findByUserIdOrderByPinnedDescCreatedAtDesc(userId)
                .stream()
                .map(session -> {
                    List<QuizItem> items = quizItemRepository.findBySessionIdOrderByIdAsc(session.getId());
                    return toResponse(session, items);
                })
                .collect(Collectors.toList());
    }

    /**
     * Deletes a quiz session and all associated items.
     */
    @Transactional
    public void deleteSession(UUID sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSession", "id", sessionId));
        quizSessionRepository.delete(session);
        log.info("Quiz session deleted with ID: {}", sessionId);
    }

    /**
     * Renames a quiz session's custom title.
     */
    @Transactional
    public QuizSessionResponse renameSession(UUID sessionId, String title) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSession", "id", sessionId));
        session.setTitle(title);
        session = quizSessionRepository.save(session);
        log.info("Quiz session renamed with ID: {} to title: {}", sessionId, title);
        
        List<QuizItem> items = quizItemRepository.findBySessionIdOrderByIdAsc(sessionId);
        return toResponse(session, items);
    }

    /**
     * Pins or unpins a quiz session.
     */
    @Transactional
    public QuizSessionResponse pinSession(UUID sessionId, boolean pinned) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSession", "id", sessionId));
        session.setPinned(pinned);
        session = quizSessionRepository.save(session);
        log.info("Quiz session pin/unpin status updated with ID: {} to: {}", sessionId, pinned);
        
        List<QuizItem> items = quizItemRepository.findBySessionIdOrderByIdAsc(sessionId);
        return toResponse(session, items);
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
                .title(session.getTitle())
                .pinned(session.isPinned())
                .personaType(session.getPersonaType())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
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
