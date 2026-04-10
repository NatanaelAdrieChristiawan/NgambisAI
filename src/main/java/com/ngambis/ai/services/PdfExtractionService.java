package com.ngambis.ai.services;

import com.ngambis.ai.exceptions.PdfParsingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for extracting text content from uploaded PDF files
 * using Apache PDFBox. Supports full-text extraction and text chunking
 * for large documents.
 */
@Service
@Slf4j
public class PdfExtractionService {

    private static final int DEFAULT_CHUNK_SIZE = 2000;
    private static final int CHUNK_OVERLAP = 200;

    /**
     * Extracts all text content from the given PDF file.
     *
     * @param file the uploaded PDF file as a MultipartFile
     * @return the extracted text content
     * @throws PdfParsingException if the PDF cannot be read or parsed
     */
    public String extractText(MultipartFile file) {
        validateFile(file);

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);

            log.info("Successfully extracted text from PDF '{}' ({} pages, {} characters)",
                    file.getOriginalFilename(),
                    document.getNumberOfPages(),
                    extractedText.length());

            return extractedText.trim();
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", file.getOriginalFilename(), e);
            throw new PdfParsingException(
                    "Failed to parse PDF file: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Extracts text from the PDF and splits it into overlapping chunks.
     * Useful for processing large documents with AI APIs that have token limits.
     *
     * @param file      the uploaded PDF file
     * @param chunkSize the maximum character count per chunk
     * @return a list of text chunks
     */
    public List<String> extractAndChunk(MultipartFile file, int chunkSize) {
        String fullText = extractText(file);
        return chunkText(fullText, chunkSize);
    }

    /**
     * Extracts text and chunks with default chunk size.
     *
     * @param file the uploaded PDF file
     * @return a list of text chunks
     */
    public List<String> extractAndChunk(MultipartFile file) {
        return extractAndChunk(file, DEFAULT_CHUNK_SIZE);
    }

    /**
     * Splits a large text into overlapping chunks for better context preservation.
     *
     * @param text      the full text to chunk
     * @param chunkSize the maximum character count per chunk
     * @return a list of overlapping text chunks
     */
    private List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            // Try to break at a sentence or word boundary
            if (end < text.length()) {
                int lastPeriod = text.lastIndexOf('.', end);
                int lastNewline = text.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastPeriod, lastNewline);

                if (breakPoint > start) {
                    end = breakPoint + 1;
                }
            }

            chunks.add(text.substring(start, end).trim());
            start = end - CHUNK_OVERLAP;

            if (start >= text.length()) break;
        }

        log.debug("Split text into {} chunks (chunk size: {}, overlap: {})",
                chunks.size(), chunkSize, CHUNK_OVERLAP);

        return chunks;
    }

    /**
     * Validates that the uploaded file is a valid PDF.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PdfParsingException("Uploaded file is empty or null.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new PdfParsingException(
                    "Invalid file type. Expected 'application/pdf' but received: " + contentType);
        }
    }
}
