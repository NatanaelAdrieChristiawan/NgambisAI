package com.ngambis.ai.exceptions;

/**
 * Exception thrown when PDF parsing/extraction fails.
 */
public class PdfParsingException extends RuntimeException {

    public PdfParsingException(String message) {
        super(message);
    }

    public PdfParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
