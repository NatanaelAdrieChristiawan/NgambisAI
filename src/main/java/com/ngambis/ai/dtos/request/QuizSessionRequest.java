package com.ngambis.ai.dtos.request;

import com.ngambis.ai.models.PersonaType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new quiz/exam session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSessionRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Document ID is required")
    private UUID documentId;

    @NotNull(message = "Persona type is required")
    private PersonaType personaType;
}
