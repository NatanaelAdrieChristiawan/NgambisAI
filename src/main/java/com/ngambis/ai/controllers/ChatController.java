package com.ngambis.ai.controllers;

import com.ngambis.ai.dtos.request.ChatRequest;
import com.ngambis.ai.dtos.response.ApiResponse;
import com.ngambis.ai.dtos.response.ConversationResponse;
import com.ngambis.ai.services.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Chat", description = "AI Chat APIs")
@SecurityRequirement(name = "bearer-jwt")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/message")
    @Operation(summary = "Send a message", description = "Sends a message to the AI, creating a new conversation if conversationId is null")
    public ResponseEntity<ApiResponse<ConversationResponse>> sendMessage(@Valid @RequestBody ChatRequest request) {
        log.info("POST /api/chat/message — User: {}", request.getUserId());

        ConversationResponse response = chatService.sendMessage(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", response));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "Get conversation", description = "Gets a specific conversation with all messages")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(@PathVariable UUID id) {
        log.info("GET /api/chat/conversations/{}", id);
        
        ConversationResponse response = chatService.getConversation(id);
        
        return ResponseEntity.ok(ApiResponse.success("Conversation retrieved", response));
    }

    @GetMapping("/conversations/user/{userId}")
    @Operation(summary = "Get user conversations", description = "Gets all conversations for a user (without messages)")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getUserConversations(@PathVariable UUID userId) {
        log.info("GET /api/chat/conversations/user/{}", userId);
        
        List<ConversationResponse> response = chatService.getConversationsByUser(userId);
        
        return ResponseEntity.ok(ApiResponse.success("User conversations retrieved", response));
    }

    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "Delete conversation", description = "Deletes a conversation and all its messages")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable UUID id) {
        log.info("DELETE /api/chat/conversations/{}", id);
        
        chatService.deleteConversation(id);
        
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully", null));
    }

    @PutMapping("/conversations/{id}/rename")
    @Operation(summary = "Rename conversation", description = "Renames a specific conversation's title")
    public ResponseEntity<ApiResponse<ConversationResponse>> renameConversation(
            @PathVariable UUID id,
            @RequestParam String title) {
        log.info("PUT /api/chat/conversations/{}/rename — New title: {}", id, title);
        
        ConversationResponse response = chatService.renameConversation(id, title);
        
        return ResponseEntity.ok(ApiResponse.success("Conversation renamed successfully", response));
    }

    @PutMapping("/conversations/{id}/pin")
    @Operation(summary = "Pin conversation", description = "Pins or unpins a specific conversation")
    public ResponseEntity<ApiResponse<ConversationResponse>> pinConversation(
            @PathVariable UUID id,
            @RequestParam boolean isPinned) {
        log.info("PUT /api/chat/conversations/{}/pin — Pinned: {}", id, isPinned);
        
        ConversationResponse response = chatService.pinConversation(id, isPinned);
        
        return ResponseEntity.ok(ApiResponse.success("Conversation pin status updated successfully", response));
    }
}
