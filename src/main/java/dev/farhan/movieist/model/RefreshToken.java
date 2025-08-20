package dev.farhan.movieist.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    private String tokenHash;

    // 🔹 CHANGED: Instead of just userId (String), reference User directly if you want to load user easily
    private ObjectId userId; // keep it String if you don't want DBRef

    // ⏰ TTL index: Mongo will delete automatically after expiryDate
    @Indexed(expireAfter = "0s") // CHANGED: correct attribute name for Spring Data MongoDB TTL
    private Instant expiresAt;

    private String userAgentHash;
    private String ipAddress;

    private boolean revoked;

    private Instant createdAt;
    private Instant lastUsedAt;
}

