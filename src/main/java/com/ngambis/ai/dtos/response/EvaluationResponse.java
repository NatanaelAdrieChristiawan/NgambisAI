package com.ngambis.ai.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for evaluation results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResponse {

    private UUID id;
    private UUID quizItemId;
    private String questionText;
    private String studentAudioTranscript;
    private Integer score;
    private String feedback;
    private LocalDateTime createdAt;
}
