package com.dts.practice.simulation;

import com.dts.practice.security.JwtUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SimulationExamAccessTest {
    final SimulationTokens tokens=new SimulationTokens(SimulationFixtures.PROPS);
    final SimulationExamAccess access=new SimulationExamAccess(new SimulationAccess(tokens),tokens);
    @AfterEach void clear(){SecurityContextHolder.clearContext();RequestContextHolder.resetRequestAttributes();}
    void context(UUID subject,String token,String...rights){
        var authorities=Arrays.stream(rights).map(r->new SimpleGrantedAuthority("PERM_"+r)).toList();var principal=new JwtUserDetails(subject,"fixture",authorities);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal,null,authorities));var request=new MockHttpServletRequest();request.addHeader("Authorization","Bearer "+token);RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
    @Test void examDoesNotRequireLearningPermission(){String[] rights={"simulations:read","simulations:exam","media:read"};var token=SimulationFixtures.access(SimulationFixtures.USER,rights);context(SimulationFixtures.USER,token,rights);assertEquals(SimulationFixtures.USER,access.require(true).userId());}
    @Test void adminAndStudentPermissionsCanCoexistWithOfficialUnderscorePermission() {
        String[] rights = {"simulations:read", "simulations:exam", "media:read", "exam_session:read"};
        context(SimulationFixtures.USER, SimulationFixtures.access(SimulationFixtures.USER, rights), rights);
        assertEquals(SimulationFixtures.USER, access.require(true).userId());
    }

    @Test void malformedPermissionIsStillForbidden() {
        String[] rights = {"simulations:read", "simulations:exam", "media:read", "forged: read"};
        context(SimulationFixtures.USER, SimulationFixtures.access(SimulationFixtures.USER, rights), rights);
        assertThrows(RuntimeException.class, () -> access.require(true));
    }

    @ParameterizedTest @ValueSource(strings={"raw-exam","principal-exam","raw-media","subject","anonymous"}) void principalAndOriginalJwtMustBothAuthorizeTheActor(String mismatch){
        List<String> raw=new ArrayList<>(List.of("simulations:read","simulations:exam","media:read"));List<String> spring=new ArrayList<>(raw);UUID subject=SimulationFixtures.USER;
        switch(mismatch){case "raw-exam"->raw.remove("simulations:exam");case "principal-exam"->spring.remove("simulations:exam");case "raw-media"->raw.remove("media:read");case "subject"->subject=UUID.randomUUID();}
        context(subject,SimulationFixtures.access(SimulationFixtures.USER,raw.toArray(String[]::new)),spring.toArray(String[]::new));if(mismatch.equals("anonymous"))SecurityContextHolder.clearContext();assertThrows(RuntimeException.class,()->access.require(true));
    }
}
