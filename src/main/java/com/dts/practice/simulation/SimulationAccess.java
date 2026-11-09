package com.dts.practice.simulation;

import com.dts.practice.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.*;

@Component
@RequiredArgsConstructor
public class SimulationAccess {
    private final SimulationTokens tokens;
    Actor learn() {
        Actor actor=read(false);
        Object rights=tokens.access(actor.accessToken()).get("permissions");
        if(!(rights instanceof Collection<?> permissions) || !permissions.contains("simulations:learn")) throw SimulationFailure.forbidden();
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth.getAuthorities().stream().noneMatch(a->"PERM_simulations:learn".equals(a.getAuthority()))) throw SimulationFailure.forbidden();
        return actor;
    }
    Actor read(boolean media) {
        var context=SecurityContextHolder.getContext().getAuthentication();
        if(context==null || !context.isAuthenticated() || !(context.getPrincipal() instanceof JwtUserDetails user)
                || !(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes))
            throw SimulationFailure.unauthorized();
        var headers=Collections.list(attributes.getRequest().getHeaders("Authorization"));
        if(headers.size()!=1 || !headers.getFirst().startsWith("Bearer ") || headers.getFirst().length()>8192) throw SimulationFailure.unauthorized();
        String bearer=headers.getFirst().substring(7);
        var claims=tokens.access(bearer);
        if(!user.userId().equals(SimulationTokens.canonical(claims.getSubject()))) throw SimulationFailure.unauthorized();
        Object raw=claims.get("permissions");
        if(!(raw instanceof Collection<?> permissions) || permissions.size()>500
                || !permissions.contains("simulations:read") || (media && !permissions.contains("media:read"))) throw SimulationFailure.forbidden();
        for(Object p:permissions) if(!(p instanceof String s) || !s.matches("[a-z][a-z0-9_-]*:[a-z][a-z0-9_-]*")) throw SimulationFailure.forbidden();
        return new Actor(user.userId(),bearer);
    }
    record Actor(UUID userId,String accessToken) {
        @Override public String toString() {return "SimulationActor[redacted]";}
    }
}
