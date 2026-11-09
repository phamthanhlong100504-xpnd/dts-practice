package com.dts.practice.adminmonitor;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminPresetAuditStreamTest {
    private final AdminPresetAuditStreamController.AuditReader reader =
        mock(AdminPresetAuditStreamController.AuditReader.class);
    private final AdminPresetAuditStreamController api = secured(reader);

    private static AdminPresetAuditStreamController secured(AdminPresetAuditStreamController.AuditReader reader) {
        var proxy = new ProxyFactory(new AdminPresetAuditStreamController(reader));
        proxy.setProxyTargetClass(true);
        proxy.addAdvisor(AuthorizationManagerBeforeMethodInterceptor.preAuthorize());
        return (AdminPresetAuditStreamController)proxy.getProxy();
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void auditPermissionIsIndependentFromReportsAndEditing() {
        auth("ROLE_ADMIN", "PERM_reports:read", "PERM_practice-exam-sets:update");
        assertThrows(AccessDeniedException.class, () -> api.list(null,null,null,null,0,30));
        auth("ROLE_STUDENT", "PERM_audit:read");
        assertThrows(AccessDeniedException.class, () -> api.list(null,null,null,null,0,30));
        auth("ROLE_ADMIN");
        assertThrows(AccessDeniedException.class, () -> api.list(null,null,null,null,0,30));
        verifyNoInteractions(reader);
    }
    @Test void permittedAdminReadsNoStoreWithoutMutation() {
        auth("ROLE_ADMIN", "PERM_audit:read");
        when(reader.list(null,null,null,null,0,30)).thenReturn(Map.of("sourceService", "dts-practice"));
        var reply=api.list(null,null,null,null,0,30);
        assertEquals("no-store",reply.getHeaders().getFirst("Cache-Control"));
        assertEquals("dts-practice",reply.getBody().get("sourceService"));
        verify(reader).list(null,null,null,null,0,30);
        verifyNoMoreInteractions(reader);
    }
    @Test void invalidFiltersAreRejectedBeforeJdbcAndAuditQueryContainsNoSecrets() {
        var db=mock(JdbcTemplate.class);
        var source=new AdminPresetAuditStreamController.AuditReader(db);
        Instant instant=Instant.parse("2026-11-01T00:00:00Z");
        assertThrows(com.dts.practice.exception.BusinessException.class, () -> source.list(instant,instant,null,null,0,30));
        assertThrows(com.dts.practice.exception.BusinessException.class, () -> source.list(null,null,null,"RELEASE;DROP",0,30));
        assertThrows(com.dts.practice.exception.BusinessException.class, () -> source.list(null,null,null,null,-1,30));
        assertThrows(com.dts.practice.exception.BusinessException.class, () -> source.list(null,null,null,null,0,101));
        verifyNoInteractions(db);

        when(db.queryForList(anyString(),any(Object[].class))).thenReturn(List.of(Map.of("auditId", "a1")));
        var output=source.list(null,null,null,"PUBLISH",0,2);
        assertEquals("READY",output.get("dataStatus"));
        assertEquals("dts-practice",output.get("sourceService"));
        assertEquals(false,output.get("hasMore"));
        assertEquals(1,((List<?>)output.get("items")).size());
        var sql=org.mockito.ArgumentCaptor.forClass(String.class);
        verify(db).queryForList(sql.capture(),any(Object[].class));
        String query=sql.getValue().toLowerCase();
        assertTrue(query.contains("admin_preset_draft_audit"));
        assertTrue(query.contains("admin_preset_release_audit"));
        for(String field:List.of("select *", "details", "payload", "fingerprint", "password", "jwt"))
            assertFalse(query.contains(field),field);
        assertTrue(query.contains("limit ? offset ?"));
    }
    @Test void malformedTypedQueryIsHttp400WithNoStore() {
        var result = new AdminPresetAuditStreamController(reader).invalidQueryType(null);
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("no-store", result.getHeaders().getFirst("Cache-Control"));
        assertEquals("BAD_REQUEST", result.getBody().code());
        verifyNoInteractions(reader);
    }
    @Test void extraRowMeansNextPageWithoutReturningAnUnboundedList() {
        var db=mock(JdbcTemplate.class);
        var source=new AdminPresetAuditStreamController.AuditReader(db);
        when(db.queryForList(anyString(),any(Object[].class))).thenReturn(List.of(
            Map.of("auditId","a"),Map.of("auditId","b")));
        var out=source.list(null,null,null,null,0,1);
        assertEquals(true,out.get("hasMore"));
        assertEquals(1,((List<?>)out.get("items")).size());
    }
    private static void auth(String... grants) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(UUID.randomUUID(),null,
                Arrays.stream(grants).map(SimpleGrantedAuthority::new).toList()));
    }
}
