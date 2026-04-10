package com.ngambis.ai.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for individual quiz items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizItemResponse {

    private UUID id;
    private String itemType;
    private String questionText;
    private String referenceText;

    // For MultipleChoiceItem
    private String options;
    private String correctAnswer;
}
