package com.ngambis.ai.controllers;

import com.ngambis.ai.dtos.response.ApiResponse;
import com.ngambis.ai.dtos.response.DocumentResponse;
import com.ngambis.ai.services.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for document management operations.
 * Handles PDF upload, text extraction, and document retrieval.
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Upload a PDF document, extract its text, and save it.
     *
     * @param userId the user ID (form field)
     * @param file   the PDF file (multipart)
     * @return the saved document with extracted text preview
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @RequestParam("userId") UUID userId,
            @RequestParam("file") MultipartFile file) {

        log.info("POST /api/documents — Upload PDF for user: {}", userId);

        DocumentResponse response = documentService.uploadAndExtract(userId, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded and text extracted successfully.", response));
    }

    /**
     * Get all documents for a specific user.
     *
     * @param userId the user ID
     * @return list of documents
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocumentsByUser(
            @PathVariable UUID userId) {

        log.info("GET /api/documents/user/{}", userId);

        List<DocumentResponse> documents = documentService.getDocumentsByUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Documents retrieved successfully.", documents));
    }

    /**
     * Get a single document by its ID.
     *
     * @param documentId the document ID
     * @return the document details
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @PathVariable UUID documentId) {

        log.info("GET /api/documents/{}", documentId);

        DocumentResponse document = documentService.getDocument(documentId);

        return ResponseEntity.ok(
                ApiResponse.success("Document retrieved successfully.", document));
    }

    /**
     * Delete a single document by its ID.
     *
     * @param documentId the document ID
     * @return success message
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable UUID documentId) {

        log.info("DELETE /api/documents/{}", documentId);

        documentService.deleteDocument(documentId);

        return ResponseEntity.ok(
                ApiResponse.success("Document deleted successfully.", null));
    }
}
