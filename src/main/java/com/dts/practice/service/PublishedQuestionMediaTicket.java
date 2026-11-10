package com.dts.practice.service;

import com.dts.practice.config.JwtValidationProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/** Short-lived, per-actor, per-image proof; not a replacement for the caller's access JWT. */
@Component
public class PublishedQuestionMediaTicket {
    private final javax.crypto.SecretKey ticketKey;

    public PublishedQuestionMediaTicket(JwtValidationProperties properties) {
        try {
            String secret = properties.accessSecret();
            if (secret == null || secret.isBlank()) throw new IllegalArgumentException("JWT secret missing");
            byte[] root;
            try { root = Decoders.BASE64.decode(secret); }
            catch (Exception e) {
                try { root = Decoders.BASE64URL.decode(secret); }
                catch (Exception ignored) { root = secret.getBytes(StandardCharsets.UTF_8); }
            }
            if (root.length < 32) root = MessageDigest.getInstance("SHA-256").digest(root);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(root, "HmacSHA256"));
            ticketKey = Keys.hmacShaKeyFor(mac.doFinal("dts-media:published-question-image:v1".getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException("Cannot configure published-question image proof", e); }
    }

    public String issue(UUID actorId, UUID mediaId) {
        Instant now = Instant.now();
        return Jwts.builder()
            .header().type("dts-published-question-image+jwt").and()
            .issuer("dts-practice").subject(actorId.toString())
            .audience().add("dts-media").and()
            .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(45)))
            .id(UUID.randomUUID().toString())
            .claim("purpose", "PUBLISHED_QUESTION_IMAGE")
            .claim("bankVersion", CurrentQuestionBank.currentVersion())
            .claim("mediaId", mediaId.toString())
            .signWith(ticketKey, Jwts.SIG.HS256).compact();
    }
}
