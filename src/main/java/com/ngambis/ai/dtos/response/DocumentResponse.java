package com.ngambis.ai.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for document-related API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    private UUID id;
    private UUID userId;
    private String filename;
    private String extractedTextPreview;
    private Integer extractedTextLength;
    private LocalDateTime uploadedAt;
}
