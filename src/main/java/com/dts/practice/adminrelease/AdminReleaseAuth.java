package com.dts.practice.adminrelease;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Dedicated shared-secret HMAC, independent of user JWT and the simulation source token. */
@Component
public final class AdminReleaseAuth {
    private final byte[] secret;
    public AdminReleaseAuth(@Value("${DTS_ADMIN_RELEASE_SHARED_SECRET:}") String secret) {
        this.secret=secret==null?new byte[0]:secret.getBytes(StandardCharsets.UTF_8);
    }
    public void require(String method,String path,String time,String signature,String body) {
        if(secret.length<32 || time==null || signature==null || !signature.matches("[a-f0-9]{64}") ||
           time.length()>16 || body==null) throw new SecurityException("Release internal authentication denied");
        try {
            long t=Long.parseLong(time);
            if(Math.abs(Instant.now().getEpochSecond()-t)>60)
                throw new SecurityException("Release internal authentication expired");
            String expected=sign(secret,method,path,time,body);
            if(!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),signature.getBytes(StandardCharsets.US_ASCII)))
                throw new SecurityException("Release internal authentication denied");
        } catch(NumberFormatException exception) {throw new SecurityException("Release internal authentication denied");}
    }
    static String sign(byte[] secret,String method,String path,String time,String body) {
        try {
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8));
            String canonical=method+"\n"+path+"\n"+time+"\n"+HexFormat.of().formatHex(digest);
            Mac mac=Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret,"HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch(Exception exception) {throw new IllegalStateException("Cannot validate internal release",exception);}
    }
}
