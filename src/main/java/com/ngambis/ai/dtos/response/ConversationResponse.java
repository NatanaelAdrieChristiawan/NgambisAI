package com.ngambis.ai.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {
    private UUID id;
    private UUID userId;
    private String title;
    private boolean pinned;
    private List<DocumentResponse> documents;
    private List<ChatMessageResponse> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
