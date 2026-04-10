package com.ngambis.ai.services;

import com.ngambis.ai.dtos.response.DocumentResponse;
import com.ngambis.ai.exceptions.ResourceNotFoundException;
import com.ngambis.ai.models.Document;
import com.ngambis.ai.models.User;
import com.ngambis.ai.repositories.DocumentRepository;
import com.ngambis.ai.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling document upload, PDF text extraction, and persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final PdfExtractionService pdfExtractionService;

    private static final int TEXT_PREVIEW_LENGTH = 500;

    /**
     * Uploads a PDF, extracts its text content, and saves the document entity.
     *
     * @param userId the ID of the user uploading the document
     * @param file   the PDF file
     * @return the saved document response
     */
    @Transactional
    public DocumentResponse uploadAndExtract(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        log.info("Processing PDF upload for user '{}': {}", user.getUsername(), file.getOriginalFilename());

        // Extract text from PDF
        String extractedText = pdfExtractionService.extractText(file);

        // Build and save document entity
        Document document = Document.builder()
                .user(user)
                .filename(file.getOriginalFilename())
                .extractedText(extractedText)
                .build();

        Document saved = documentRepository.save(document);
        log.info("Document saved with ID: {}", saved.getId());

        return toResponse(saved);
    }

    /**
     * Retrieves all documents for a given user, ordered by upload date (newest first).
     */
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        return documentRepository.findByUserIdOrderByUploadedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single document by its ID.
     */
    @Transactional(readOnly = true)
    public DocumentResponse getDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        return toResponse(document);
    }

    /**
     * Maps a Document entity to a DocumentResponse DTO.
     */
    private DocumentResponse toResponse(Document document) {
        String textPreview = null;
        Integer textLength = null;

        if (document.getExtractedText() != null) {
            textLength = document.getExtractedText().length();
            textPreview = document.getExtractedText().length() > TEXT_PREVIEW_LENGTH
                    ? document.getExtractedText().substring(0, TEXT_PREVIEW_LENGTH) + "..."
                    : document.getExtractedText();
        }

        return DocumentResponse.builder()
                .id(document.getId())
                .userId(document.getUser().getId())
                .filename(document.getFilename())
                .extractedTextPreview(textPreview)
                .extractedTextLength(textLength)
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}
