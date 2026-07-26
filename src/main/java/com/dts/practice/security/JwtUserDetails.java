package com.dts.practice.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public record JwtUserDetails(
        UUID userId,
        String username,
        Collection<? extends GrantedAuthority> authorities
) {}
