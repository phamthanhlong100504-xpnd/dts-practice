package com.dts.practice.controller;

import com.dts.practice.config.JwtValidationProperties;
import com.dts.practice.repository.QuestionRepository;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.CurrentQuestionBank;
import com.dts.practice.service.PublishedQuestionMediaTicket;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PublishedQuestionMediaControllerTest {
    private final QuestionRepository bank = mock(QuestionRepository.class);
    private final PublishedQuestionMediaController controller = new PublishedQuestionMediaController(bank,
        new PublishedQuestionMediaTicket(new JwtValidationProperties(
            java.util.Base64.getEncoder().encodeToString("published-test-secret-012345678901234567890123456789".getBytes()),
            "test-issuer")));
    @AfterEach void clear(){ SecurityContextHolder.clearContext(); }

    private void actor(UUID id) {
        var rights = List.of(new SimpleGrantedAuthority("PERM_practice:read"), new SimpleGrantedAuthority("PERM_media:read"));
        var principal = new JwtUserDetails(id, "learner", rights);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, rights));
    }
    @Test void onlyAReferencedCurrentPublishedBankImageCanReceiveProof() {
        UUID image = UUID.randomUUID(); actor(UUID.randomUUID());
        assertThrows(ResponseStatusException.class, () -> controller.issue(image));
        verify(bank).existsByBankVersionAndMediaFileId(CurrentQuestionBank.currentVersion(), image);
        when(bank.existsByBankVersionAndMediaFileId(CurrentQuestionBank.currentVersion(), image)).thenReturn(true);
        var reply = controller.issue(image);
        assertEquals(200, reply.getStatusCode().value());
        assertNotNull(reply.getBody());
        assertFalse(reply.getBody().get("ticket").isBlank());
        assertTrue(reply.getHeaders().getCacheControl().contains("no-store"));
    }
    @Test void anonymousCannotReceiveProof() {
        var id = UUID.randomUUID();
        when(bank.existsByBankVersionAndMediaFileId(CurrentQuestionBank.currentVersion(), id)).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> controller.issue(id));
    }
}
