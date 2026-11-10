package com.dts.practice.simulation;

import com.dts.practice.config.JwtValidationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Component
public class SimulationTokens {
    private final String encodedSecret;
    private final SecretKey accessKey;
    private final SecretKey thumbnailKey;
    private final SecretKey playbackKey;
    private final String issuer;
    public SimulationTokens(JwtValidationProperties properties) {
        encodedSecret=properties.accessSecret();issuer=properties.issuer();
        byte[] root=decode(encodedSecret);
        accessKey=Keys.hmacShaKeyFor(root);
        thumbnailKey=derive(root,"dts-media:simulation-catalog-thumbnail:v1");
        playbackKey=derive(root,"dts-media:simulation-playback:v1");
    }
    Claims access(String token) {
        try {
            var claims=Jwts.parser().verifyWith(accessKey).requireIssuer(issuer).build().parseSignedClaims(token).getPayload();
            if(claims.getExpiration()==null || claims.getIssuedAt()==null || claims.getIssuedAt().toInstant().isAfter(Instant.now())
                    || !claims.getExpiration().after(claims.getIssuedAt())
                    || (claims.get("type")!=null && !"access".equals(claims.get("type")))) throw new IllegalArgumentException();
            canonical(claims.getSubject());return claims;
        } catch(Exception e) {throw SimulationFailure.unauthorized();}
    }
    String source() {
        try {
            // Content Builder's source protocol deliberately requires Base64 root bytes.
            byte[] root=Decoders.BASE64.decode(encodedSecret);
            if(root.length<32) throw new IllegalArgumentException();
            var now=Instant.now();
            return Jwts.builder().header().type("dts-service+jwt").and().issuer("dts-practice").subject("simulation-bank-reader")
                    .audience().add("dts-content-builder").and().issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(45)))
                    .id(UUID.randomUUID().toString()).claim("purpose","SIMULATION_SOURCE_READ")
                    .signWith(derive(root,"dts:content-builder:simulation-source:v1"),Jwts.SIG.HS256).compact();
        } catch(Exception e) {throw SimulationFailure.unavailable();}
    }
    String thumbnail(UUID user,SimulationBank.Snapshot bank,SimulationBank.Scenario scenario) {
        // Only callers that obtained an authenticated published snapshot can reach here.
        var now=Instant.now();
        return Jwts.builder().header().type("dts-simulation-catalog+jwt").and().issuer("dts-practice").subject(user.toString())
                .audience().add("dts-media").and().issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(45)))
                .id(UUID.randomUUID().toString()).claim("purpose","SIMULATION_CATALOG_THUMBNAIL")
                .claim("bankVersion",bank.bankVersion()).claim("scenarioId",scenario.scenarioId().toString())
                .claim("mediaId",scenario.thumbnailMediaId().toString()).claim("targetType","SIMULATION_THUMBNAIL")
                .signWith(thumbnailKey,Jwts.SIG.HS256).compact();
    }
    static UUID canonical(String value) {
        UUID id=UUID.fromString(Objects.requireNonNull(value));
        if(!id.toString().equalsIgnoreCase(value)) throw new IllegalArgumentException();return id;
    }
    String playback(UUID user,SimulationLearningModel.Row attempt) {
        var now=Instant.now();
        return Jwts.builder().issuer("dts-practice").subject(user.toString()).audience().add("dts-media").and()
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(60))).id(UUID.randomUUID().toString())
                .claim("purpose","SIMULATION_LEARNING").claim("bankVersion",attempt.bank).claim("attemptId",attempt.id.toString())
                .claim("mediaId",attempt.snapshot.videoMediaId().toString()).claim("targetType","SIMULATION_VIDEO")
                .signWith(playbackKey,Jwts.SIG.HS256).compact();
    }
    private static byte[] decode(String value) {
        if(value==null || value.isBlank()) throw new IllegalArgumentException("JWT secret must not be empty");
        byte[] bytes;
        try {bytes=Decoders.BASE64.decode(value);} catch(Exception e) {
            try {bytes=Decoders.BASE64URL.decode(value);} catch(Exception ignored) {bytes=value.getBytes(StandardCharsets.UTF_8);}
        }
        try {return bytes.length<32?MessageDigest.getInstance("SHA-256").digest(bytes):bytes;}
        catch(Exception e) {throw new IllegalStateException("Cannot initialize simulation access authentication");}
    }
    private static SecretKey derive(byte[] root,String context) {
        try {
            Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(root,"HmacSHA256"));
            return Keys.hmacShaKeyFor(mac.doFinal(context.getBytes(StandardCharsets.UTF_8)));
        } catch(Exception e) {throw new IllegalStateException("Cannot initialize simulation service authentication");}
    }
}
