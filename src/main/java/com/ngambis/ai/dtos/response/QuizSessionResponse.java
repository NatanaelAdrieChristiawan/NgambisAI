package com.ngambis.ai.dtos.response;

import com.ngambis.ai.models.PersonaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for quiz session data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSessionResponse {

    private UUID id;
    private UUID userId;
    private UUID documentId;
    private String documentFilename;
    private PersonaType personaType;
    private LocalDateTime createdAt;
    private List<QuizItemResponse> quizItems;
}
