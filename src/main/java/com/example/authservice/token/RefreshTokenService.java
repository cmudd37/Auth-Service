package com.example.authservice.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final RefreshTokenProperties properties;

    public RefreshTokenService(StringRedisTemplate redisTemplate, RefreshTokenProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public String create(UUID userId) {
        UUID tokenId = UUID.randomUUID();
        String secret = randomSecret();
        String key = key(tokenId);
        redisTemplate.opsForHash().putAll(key, Map.of(
                "userId", userId.toString(),
                "secretHash", sha256(secret),
                "expiresAt", Instant.now().plus(properties.ttl()).toString(),
                "revoked", "false"
        ));
        redisTemplate.expire(key, properties.ttl());
        return tokenId + "." + secret;
    }

    public Rotation rotate(String rawToken) {
        ParsedToken parsed = parse(rawToken);
        String key = key(parsed.id());
        Map<Object, Object> values = redisTemplate.opsForHash().entries(key);
        if (values.isEmpty() || Boolean.parseBoolean(String.valueOf(values.get("revoked")))) {
            throw new InvalidRefreshTokenException();
        }
        Instant expiresAt = Instant.parse(String.valueOf(values.get("expiresAt")));
        if (expiresAt.isBefore(Instant.now())) {
            redisTemplate.delete(key);
            throw new InvalidRefreshTokenException();
        }
        if (!MessageDigest.isEqual(
                sha256(parsed.secret()).getBytes(StandardCharsets.UTF_8),
                String.valueOf(values.get("secretHash")).getBytes(StandardCharsets.UTF_8)
        )) {
            throw new InvalidRefreshTokenException();
        }

        redisTemplate.opsForHash().put(key, "revoked", "true");
        UUID userId = UUID.fromString(String.valueOf(values.get("userId")));
        return new Rotation(userId, create(userId));
    }

    public void revoke(String rawToken) {
        ParsedToken parsed = parse(rawToken);
        redisTemplate.opsForHash().put(key(parsed.id()), "revoked", "true");
    }

    private ParsedToken parse(String rawToken) {
        String[] parts = rawToken == null ? new String[0] : rawToken.split("\\.", 2);
        if (parts.length != 2) {
            throw new InvalidRefreshTokenException();
        }
        try {
            return new ParsedToken(UUID.fromString(parts[0]), parts[1]);
        } catch (IllegalArgumentException ex) {
            throw new InvalidRefreshTokenException();
        }
    }

    private String randomSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String key(UUID tokenId) {
        return KEY_PREFIX + tokenId;
    }

    public record Rotation(UUID userId, String refreshToken) {
    }

    private record ParsedToken(UUID id, String secret) {
    }
}
