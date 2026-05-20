package com.ngambis.ai.services;

import com.ngambis.ai.dtos.request.ChatRequest;
import com.ngambis.ai.dtos.response.ChatMessageResponse;
import com.ngambis.ai.dtos.response.ConversationResponse;
import com.ngambis.ai.dtos.response.DocumentResponse;
import com.ngambis.ai.exceptions.ResourceNotFoundException;
import com.ngambis.ai.models.ChatMessage;
import com.ngambis.ai.models.Conversation;
import com.ngambis.ai.models.Document;
import com.ngambis.ai.models.User;
import com.ngambis.ai.repositories.ChatMessageRepository;
import com.ngambis.ai.repositories.ConversationRepository;
import com.ngambis.ai.repositories.DocumentRepository;
import com.ngambis.ai.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final AiIntegrationService aiIntegrationService;

    @Transactional
    public ConversationResponse sendMessage(ChatRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Conversation conversation;

        if (request.getConversationId() != null) {
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", request.getConversationId()));
        } else {
            if (request.getDocumentIds() == null || request.getDocumentIds().isEmpty()) {
                throw new IllegalArgumentException("documentIds required for new conversation");
            }
            List<Document> documents = documentRepository.findAllById(request.getDocumentIds());
            if (documents.isEmpty()) {
                throw new ResourceNotFoundException("Document", "ids", request.getDocumentIds());
            }

            conversation = Conversation.builder()
                    .user(user)
                    .documents(documents)
                    // Take first 30 chars of first message as title
                    .title(request.getMessage().length() > 30 ? request.getMessage().substring(0, 30) + "..." : request.getMessage())
                    .build();
            conversation = conversationRepository.save(conversation);
        }

        // Save user message
        ChatMessage userMessage = ChatMessage.builder()
                .conversation(conversation)
                .role("user")
                .content(request.getMessage())
                .build();
        chatMessageRepository.save(userMessage);

        // Build history for AI (last 10 messages)
        List<ChatMessage> historyMessages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        List<Map<String, Object>> aiHistory = historyMessages.stream()
                .filter(m -> !m.getId().equals(userMessage.getId())) // exclude current
                .map(m -> Map.<String, Object>of(
                        "role", "model".equals(m.getRole()) ? "model" : "user",
                        "parts", List.of(Map.of("text", m.getContent()))
                ))
                .collect(Collectors.toList());

        // Context from documents
        String context = conversation.getDocuments().stream()
                .map(Document::getExtractedText)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining("\n\n---\n\n"));

        String aiPrompt = String.format("Gunakan dokumen berikut sebagai konteks jika relevan.\n\nKonteks Dokumen:\n%s\n\nPertanyaan User: %s",
                context.length() > 10000 ? context.substring(0, 10000) : context,
                request.getMessage());

        // Call Gemini
        String aiResponseText = aiIntegrationService.chatMessage(aiPrompt, aiHistory);

        // Save AI response
        ChatMessage aiMessage = ChatMessage.builder()
                .conversation(conversation)
                .role("model")
                .content(aiResponseText)
                .build();
        chatMessageRepository.save(aiMessage);

        return getConversation(conversation.getId());
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        return toResponse(conv, messages);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationsByUser(UUID userId) {
        return conversationRepository.findByUserIdOrderByPinnedDescUpdatedAtDesc(userId)
                .stream()
                .map(conv -> toResponse(conv, new ArrayList<>())) // omit messages for list view
                .collect(Collectors.toList());
    }

    /**
     * Deletes a conversation and all its messages.
     */
    @Transactional
    public void deleteConversation(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        conversationRepository.delete(conversation);
        log.info("Conversation deleted with ID: {}", conversationId);
    }

    @Transactional
    public ConversationResponse renameConversation(UUID conversationId, String title) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        conversation.setTitle(title);
        conversation = conversationRepository.save(conversation);
        log.info("Conversation renamed with ID: {} to title: {}", conversationId, title);
        return toResponse(conversation, new ArrayList<>());
    }

    @Transactional
    public ConversationResponse pinConversation(UUID conversationId, boolean isPinned) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        conversation.setPinned(isPinned);
        conversation = conversationRepository.save(conversation);
        log.info("Conversation pinned/unpinned with ID: {} to: {}", conversationId, isPinned);
        return toResponse(conversation, new ArrayList<>());
    }

    private ConversationResponse toResponse(Conversation conv, List<ChatMessage> messages) {
        List<DocumentResponse> docResponses = conv.getDocuments().stream()
                .map(d -> DocumentResponse.builder()
                        .id(d.getId())
                        .filename(d.getFilename())
                        .build())
                .collect(Collectors.toList());

        List<ChatMessageResponse> msgResponses = messages.stream()
                .map(m -> ChatMessageResponse.builder()
                        .id(m.getId())
                        .role(m.getRole())
                        .content(m.getContent())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ConversationResponse.builder()
                .id(conv.getId())
                .userId(conv.getUser().getId())
                .title(conv.getTitle())
                .pinned(conv.isPinned())
                .documents(docResponses)
                .messages(msgResponses)
                .createdAt(conv.getCreatedAt())
                .updatedAt(conv.getUpdatedAt())
                .build();
    }
}
