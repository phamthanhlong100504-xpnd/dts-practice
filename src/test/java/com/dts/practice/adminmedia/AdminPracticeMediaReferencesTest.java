package com.dts.practice.adminmedia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

class AdminPracticeMediaReferencesTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final AdminPracticeMediaReferences service = new AdminPracticeMediaReferences(jdbc);

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    private void auth(String... grants) {
        var authorities = java.util.Arrays.stream(grants).map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(UUID.randomUUID(), null, authorities));
    }

    @Test void anonymousCannotInspectAndNeverHitsDatabase() {
        var ex = assertThrows(ResponseStatusException.class, () -> service.inspect(UUID.randomUUID()));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verifyNoInteractions(jdbc);
    }

    @Test void learnerWithMediaReadCannotInspect() {
        auth("ROLE_STUDENT", "PERM_media:read");
        assertThrows(ResponseStatusException.class, () -> service.inspect(UUID.randomUUID()));
        verifyNoInteractions(jdbc);
    }

    @Test void adminWithoutMediaPermissionCannotInspect() {
        auth("ROLE_ADMIN");
        assertThrows(ResponseStatusException.class, () -> service.inspect(UUID.randomUUID()));
        verifyNoInteractions(jdbc);
    }

    @SuppressWarnings("unchecked")
    @Test void zeroReferencesStillNeverMeansRetirementIsSafe() {
        auth("ROLE_ADMIN", "PERM_media:read");
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object.class))).thenReturn(List.of());
        var result = service.inspect(UUID.randomUUID());
        assertEquals("PRACTICE", result.source());
        assertEquals(0, result.knownReferenceCount());
        assertFalse(result.verified());
        assertFalse(result.canRetire());
        assertFalse(result.truncated());
        assertEquals("OTHER_SERVICE_REFERENCES_NOT_VERIFIED", result.reasonCode());
        verify(jdbc, times(6)).query(anyString(), any(RowMapper.class), any(Object.class));
    }

    @SuppressWarnings("unchecked")
    @Test void aKnownReferenceCannotAuthorizeRetirement() {
        auth("ROLE_ADMIN", "PERM_media:read");
        var ref = new AdminPracticeMediaReferences.Item("questions", "1", "v1", "QUESTION_MEDIA", "IMPORTED");
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object.class)))
            .thenReturn(List.of(ref), List.of(), List.of(), List.of(), List.of(), List.of());
        var result = service.inspect(UUID.randomUUID());
        assertEquals(1, result.knownReferenceCount());
        assertEquals(ref, result.references().get(0));
        assertFalse(result.verified());
        assertFalse(result.canRetire());
    }

    @SuppressWarnings("unchecked")
    @Test void reportMarksTruncationRatherThanUnderreportingAsComplete() {
        auth("ROLE_ADMIN", "PERM_media:read");
        var ref = new AdminPracticeMediaReferences.Item("questions", "1", "v1", "QUESTION_MEDIA", "IMPORTED");
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object.class)))
            .thenReturn(java.util.Collections.nCopies(501, ref), List.of(), List.of(), List.of(), List.of(), List.of());
        var result = service.inspect(UUID.randomUUID());
        assertEquals(500, result.knownReferenceCount());
        assertEquals(500, result.references().size());
        assertTrue(result.truncated());
        assertFalse(result.canRetire());
    }
}
