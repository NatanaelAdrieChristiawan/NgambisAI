package com.ngambis.ai.dtos.request;

import com.ngambis.ai.models.PersonaType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new quiz/exam session.
 * Supports multiple documents — extracted texts are merged for AI question generation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSessionRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotEmpty(message = "At least one document ID is required")
    private List<UUID> documentIds;

    @NotNull(message = "Persona type is required")
    private PersonaType personaType;

    /** Number of questions to generate. Default: 5, max: 20. */
    @Min(value = 1, message = "Question count must be at least 1")
    @Max(value = 20, message = "Question count must not exceed 20")
    @Builder.Default
    private int questionCount = 5;

    /** Question type: "MULTIPLE_CHOICE" or "ESSAY". Default: "ESSAY". */
    @Builder.Default
    private String itemType = "ESSAY";
}

