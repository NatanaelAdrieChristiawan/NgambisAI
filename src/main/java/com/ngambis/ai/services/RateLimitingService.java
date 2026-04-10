package com.ngambis.ai.services;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for managing rate limits using token bucket algorithm.
 * Prevents abuse of expensive AI operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitingService {

    private final Map<String, Bucket> rateLimitBuckets;
    private final com.ngambis.ai.config.RateLimitingConfig rateLimitingConfig;

    /**
     * Checks if the user can make an AI call (consumes a token).
     * Returns true if allowed, false if rate limit exceeded.
     */
    public boolean tryConsumeToken(String userId) {
        Bucket bucket = rateLimitBuckets.computeIfAbsent(
                userId, 
                k -> rateLimitingConfig.createBucket()
        );

        boolean consumed = bucket.tryConsume(1);
        
        if (!consumed) {
            log.warn("Rate limit exceeded for user: {}", userId);
        }

        return consumed;
    }

    /**
     * Gets the number of available tokens for a user.
     */
    public long getAvailableTokens(String userId) {
        Bucket bucket = rateLimitBuckets.get(userId);
        return bucket != null ? bucket.getAvailableTokens() : 0;
    }
}
