package com.ngambis.ai.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the parsed AI evaluation response.
 * Maps to the JSON structure returned by the AI model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiEvaluationResponse {

    /**
     * The score assigned by the AI (0-100).
     */
    private Integer score;

    /**
     * Detailed feedback from the AI model.
     */
    private String feedback;
}
