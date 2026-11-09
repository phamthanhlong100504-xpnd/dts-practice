package com.dts.practice.adminrelease;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AdminReleaseAuthTest {
    private static final String SECRET="test-only-release-key-with-at-least-32-characters";
    private static final String PATH="/api/v1/questions/internal/admin-releases/simulation/prepare";
    @Test void validInternalSignatureIsAccepted() {
        var auth=new AdminReleaseAuth(SECRET);
        String now=Long.toString(Instant.now().getEpochSecond());
        String body="{\"releaseId\":\"test\"}";
        String signature=AdminReleaseAuth.sign(SECRET.getBytes(StandardCharsets.UTF_8),"POST",PATH,now,body);
        assertDoesNotThrow(()->auth.require("POST",PATH,now,signature,body));
    }
    @Test void unauthenticatedOrChangedBodyIsRejected() {
        var auth=new AdminReleaseAuth(SECRET);
        String now=Long.toString(Instant.now().getEpochSecond());
        String signature=AdminReleaseAuth.sign(SECRET.getBytes(StandardCharsets.UTF_8),"POST",PATH,now,"{}");
        assertThrows(SecurityException.class,()->auth.require("POST",PATH,now,signature,"{ }") );
        assertThrows(SecurityException.class,()->auth.require("GET",PATH,now,signature,"{}"));
        assertThrows(SecurityException.class,()->new AdminReleaseAuth("").require("POST",PATH,now,signature,"{}"));
    }
    @Test void expiredOrMalformedSignatureIsRejected() {
        var auth=new AdminReleaseAuth(SECRET);
        String expired=Long.toString(Instant.now().minusSeconds(120).getEpochSecond());
        String sig=AdminReleaseAuth.sign(SECRET.getBytes(StandardCharsets.UTF_8),"POST",PATH,expired,"{}");
        assertThrows(SecurityException.class,()->auth.require("POST",PATH,expired,sig,"{}"));
        assertThrows(SecurityException.class,()->auth.require("POST",PATH,"nan",sig,"{}"));
    }
}
