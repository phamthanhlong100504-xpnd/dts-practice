package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class SimulationExamAccess {
    private final SimulationAccess access;private final SimulationTokens tokens;
    SimulationAccess.Actor require(boolean media) {
        var actor=access.read(media);Object raw=tokens.access(actor.accessToken()).get("permissions");
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(!(raw instanceof Collection<?> rights)||!rights.contains("simulations:exam")||auth==null
                ||auth.getAuthorities().stream().noneMatch(a->a.getAuthority().equals("PERM_simulations:exam")))throw SimulationFailure.forbidden();
        return actor;
    }
}
