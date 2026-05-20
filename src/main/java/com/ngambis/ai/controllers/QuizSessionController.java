package com.ngambis.ai.controllers;

import com.ngambis.ai.dtos.request.QuizSessionRequest;
import com.ngambis.ai.dtos.response.ApiResponse;
import com.ngambis.ai.dtos.response.QuizSessionResponse;
import com.ngambis.ai.services.QuizSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for quiz session management.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class QuizSessionController {

    private final QuizSessionService quizSessionService;

    /**
     * Create a new quiz session for a user and document.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuizSessionResponse>> createSession(
            @Valid @RequestBody QuizSessionRequest request) {

        log.info("POST /api/sessions — User: {}, Documents: {}, Persona: {}",
                request.getUserId(), request.getDocumentIds(), request.getPersonaType());

        QuizSessionResponse response = quizSessionService.createSession(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz session created successfully.", response));
    }

    /**
     * Get a quiz session by ID (includes quiz items).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizSessionResponse>> getSession(@PathVariable UUID id) {

        log.info("GET /api/sessions/{}", id);
        QuizSessionResponse session = quizSessionService.getSessionById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Quiz session retrieved successfully.", session));
    }

    /**
     * Get all sessions for a user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<QuizSessionResponse>>> getSessionsByUser(
            @PathVariable UUID userId) {

        log.info("GET /api/sessions/user/{}", userId);
        List<QuizSessionResponse> sessions = quizSessionService.getSessionsByUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User sessions retrieved successfully.", sessions));
    }

    /**
     * Delete a quiz session.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable UUID id) {
        log.info("DELETE /api/sessions/{}", id);
        quizSessionService.deleteSession(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz session deleted successfully.", null));
    }

    /**
     * Rename a quiz session.
     */
    @PutMapping("/{id}/rename")
    public ResponseEntity<ApiResponse<QuizSessionResponse>> renameSession(
            @PathVariable UUID id,
            @RequestParam String title) {
        log.info("PUT /api/sessions/{}/rename — New title: {}", id, title);
        QuizSessionResponse response = quizSessionService.renameSession(id, title);
        return ResponseEntity.ok(ApiResponse.success("Quiz session renamed successfully.", response));
    }

    /**
     * Pin or unpin a quiz session.
     */
    @PutMapping("/{id}/pin")
    public ResponseEntity<ApiResponse<QuizSessionResponse>> pinSession(
            @PathVariable UUID id,
            @RequestParam boolean isPinned) {
        log.info("PUT /api/sessions/{}/pin — Pinned: {}", id, isPinned);
        QuizSessionResponse response = quizSessionService.pinSession(id, isPinned);
        return ResponseEntity.ok(ApiResponse.success("Quiz session pin status updated successfully.", response));
    }
}
