package com.ngambis.ai.exceptions;

/**
 * Exception thrown when the AI/LLM service call fails
 * (timeout, parsing error, API error, etc.).
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
