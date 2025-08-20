package dev.farhan.movieist.repository;

import dev.farhan.movieist.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // 🔹 CHANGED: A user can have multiple refresh tokens (one per device), so return a list not Optional
    java.util.List<RefreshToken> findByUserId(String userId);

    void deleteByUserId(String userId);

    void deleteByTokenHash(String tokenHash);
}
