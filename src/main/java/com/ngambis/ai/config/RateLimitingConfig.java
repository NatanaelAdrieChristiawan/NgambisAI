package com.ngambis.ai.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for rate limiting using Bucket4j.
 * Prevents abuse of AI endpoints by limiting requests per user.
 */
@Configuration
public class RateLimitingConfig {

    /**
     * In-memory storage for rate limit buckets per user.
     * In production, consider using Redis for distributed rate limiting.
     */
    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Creates a rate limit bucket with specified limits.
     * Default: 10 calls per minute.
     */
    public Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
