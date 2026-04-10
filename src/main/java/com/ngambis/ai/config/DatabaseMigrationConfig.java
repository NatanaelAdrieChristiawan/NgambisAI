package com.ngambis.ai.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Runs database schema patches on application startup.
 *
 * <p>Hibernate's ddl-auto=update does NOT change existing columns
 * from NOT NULL to NULL. This runner handles schema changes that
 * Hibernate can't auto-apply.</p>
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class DatabaseMigrationConfig {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    public ApplicationRunner databaseMigrationRunner() {
        return args -> {
            try {
                // Make password column nullable for OAuth2 users (Google users have no password)
                jdbcTemplate.execute(
                    "ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NULL"
                );
                log.info("✅ Database migration: password column set to nullable");
            } catch (Exception e) {
                // Column might already be nullable, or table doesn't exist yet
                log.debug("Database migration skipped (may already be applied): {}", e.getMessage());
            }
        };
    }
}
