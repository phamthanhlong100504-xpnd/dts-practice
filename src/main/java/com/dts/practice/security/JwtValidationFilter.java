package com.dts.practice.security;

import com.dts.practice.config.JwtValidationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class JwtValidationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey accessKey;
    private final String issuer;

    public JwtValidationFilter(JwtValidationProperties props) {
        this.accessKey = Keys.hmacShaKeyFor(decodeSecret(props.accessSecret()));
        this.issuer = props.issuer();
    }

    // Giữ cùng quy tắc giải mã với Identity JwtProvider.
    private static byte[] decodeSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must not be empty");
        }

        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(secret);
        } catch (Exception e1) {
            try {
                bytes = Decoders.BASE64URL.decode(secret);
            } catch (Exception e2) {
                bytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        if (bytes.length < 32) {
            try {
                java.security.MessageDigest sha256 =
                        java.security.MessageDigest.getInstance("SHA-256");
                bytes = sha256.digest(bytes);
            } catch (java.security.NoSuchAlgorithmException e) {
                byte[] padded = new byte[32];
                for (int i = 0; i < 32; i++) {
                    padded[i] = bytes[i % bytes.length];
                }
                bytes = padded;
            }
        }

        return bytes;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(accessKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            String username = claims.get("username", String.class);
            List<String> roles = claims.get("roles", List.class);
            List<String> permissions = claims.get("permissions", List.class);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (roles != null) {
                roles.forEach(role ->
                        authorities.add(new SimpleGrantedAuthority(role)));
            }
            if (permissions != null) {
                permissions.forEach(permission ->
                        authorities.add(
                                new SimpleGrantedAuthority(
                                        "PERM_" + permission
                                )
                        ));
            }

            JwtUserDetails userDetails =
                    new JwtUserDetails(userId, username, authorities);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities
                    );
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header)
                && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}