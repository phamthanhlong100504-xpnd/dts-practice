package com.dts.practice.adminmonitor;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the real Spring Security method-authorization advisors without booting a
 * second application context (which would also bring in unrelated DTS beans).
 * The production controller, mapping and security annotations are unchanged.
 */
class AdminOutboxMonitorControllerTest {
    private static final UUID EVENT = new UUID(0, 10);
    private final AdminOutboxMonitorController.ReadModel report =
        mock(AdminOutboxMonitorController.ReadModel.class);
    private final AdminOutboxMonitorController api = securedController(report);

    private static AdminOutboxMonitorController securedController(
            AdminOutboxMonitorController.ReadModel readModel) {
        ProxyFactory factory = new ProxyFactory(new AdminOutboxMonitorController(readModel));
        factory.setProxyTargetClass(true);
        factory.addAdvisor(AuthorizationManagerBeforeMethodInterceptor.preAuthorize());
        return (AdminOutboxMonitorController) factory.getProxy();
    }

    @AfterEach void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test void learnerAndAdminWithoutIntegrationGrantNeverAccessProducer() {
        auth("ROLE_STUDENT", "PERM_integrations:read");
        assertThrows(AccessDeniedException.class, () -> api.outbox());
        assertThrows(AccessDeniedException.class, () -> api.event(EVENT));
        auth("ROLE_ADMIN");
        assertThrows(AccessDeniedException.class, () -> api.outbox());
        assertThrows(AccessDeniedException.class, () -> api.event(EVENT));
        auth("ROLE_ADMIN", "PERM_reports:read");
        assertThrows(AccessDeniedException.class, () -> api.outbox());
        verifyNoInteractions(report);
    }

    @Test void explicitAdminIntegrationGrantCanReadWithoutCaching() {
        auth("ROLE_ADMIN", "PERM_integrations:read");
        when(report.summary()).thenReturn(Map.of("dataStatus", "READY"));
        when(report.event(EVENT)).thenReturn(Map.of("eventId", EVENT));

        var summary = api.outbox();
        var event = api.event(EVENT);
        assertEquals("no-store", summary.getHeaders().getFirst("Cache-Control"));
        assertEquals("no-store", event.getHeaders().getFirst("Cache-Control"));
        assertEquals("READY", summary.getBody().get("dataStatus"));
        assertEquals(EVENT, event.getBody().get("eventId"));
        verify(report, times(1)).summary();
        verify(report, times(1)).event(EVENT);
        verifyNoMoreInteractions(report);
    }

    @Test void summaryIsBoundedToFiveExistingSourceOutboxesAndNeverClaimsConsumerAck() {
        var db = mock(JdbcTemplate.class);
        var source = new AdminOutboxMonitorController.ReadModel(db);
        when(db.queryForMap(anyString())).thenReturn(
            Map.of("total", 1L, "pending", 1L, "retrying", 0L));

        var result = source.summary();
        assertEquals("PARTIAL", result.get("dataStatus"));
        assertEquals(5L, result.get("sourcePending"));
        assertNull(result.get("consumerReceived"));
        assertNull(result.get("projectionApplied"));
        verify(db, times(5)).queryForMap(anyString());
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(db, times(5)).queryForMap(sql.capture());
        for (var query : sql.getAllValues()) {
            assertFalse(query.toLowerCase().contains("select *"));
            assertFalse(query.toLowerCase().contains("payload"));
            assertTrue(query.contains("published_at"));
        }
    }

    private static void auth(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                EVENT, null,
                Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList()));
    }
}
