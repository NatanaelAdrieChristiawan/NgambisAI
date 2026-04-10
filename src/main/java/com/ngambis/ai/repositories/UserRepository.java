package com.ngambis.ai.repositories;

import com.ngambis.ai.models.AuthProvider;
import com.ngambis.ai.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User} entities.
 * Supports lookup by username, email, and provider-specific queries for OAuth2.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    /**
     * Find user by email and authentication provider.
     * Useful for distinguishing LOCAL vs GOOGLE accounts with the same email.
     */
    Optional<User> findByEmailAndProvider(String email, AuthProvider provider);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
