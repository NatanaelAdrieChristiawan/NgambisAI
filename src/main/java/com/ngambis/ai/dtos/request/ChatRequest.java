package com.ngambis.ai.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for sending a message to the AI chat.
 * Can be used to create a new conversation (if conversationId is null) or reply to an existing one.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private UUID conversationId; // Null for new conversation

    private List<UUID> documentIds; // Required if conversationId is null

    @NotBlank(message = "Message content is required")
    private String message;
}
