package dev.farhan.movieist.service;

import dev.farhan.movieist.model.RefreshToken;
import dev.farhan.movieist.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.jwt.refresh-exp-hours}")
    private long refreshTokenExpirationHours;

    @Value("${app.jwt.refresh-cookie-name}")
    private String refreshCookieName;

    /** Mint a new refresh token, store the hash in MongoDB, return the plain token */
    public String mint(ObjectId userId, String userAgentHash, String ipAddress) {
        String token = generateRandomToken();
        String hash = sha256(token);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setTokenHash(hash);
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenExpirationHours, ChronoUnit.DAYS));
        refreshToken.setUserAgentHash(userAgentHash);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    /** CHANGED: Added validateAndGet (used in controller) */
    public Optional<RefreshToken> validateAndGet(String tokenValue) {
        String hash = sha256(tokenValue);
        Optional<RefreshToken> rtOpt = refreshTokenRepository.findByTokenHash(hash);

        return rtOpt.filter(rt -> !rt.isRevoked() && rt.getExpiresAt().isAfter(Instant.now()));
    }

    /** Rotate: revoke old and mint new */
    public String rotate(RefreshToken oldToken, String userAgentHash, String ipAddress) {
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        return mint(oldToken.getUserId(), userAgentHash, ipAddress);
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Value("${api.domain}")
    private String domain;

    public ResponseCookie buildRefreshCookie(String token, boolean secure) {
        return ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("None")
                .path("/")
                .domain(domain)
                .maxAge(refreshTokenExpirationHours * 60 * 60)
                .build();
    }

    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing refresh token", e);
        }
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
