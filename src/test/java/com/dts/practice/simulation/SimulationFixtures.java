package com.dts.practice.simulation;

import com.dts.practice.config.JwtValidationProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

final class SimulationFixtures {
    static final String SECRET=Base64.getEncoder().encodeToString("simulation-practice-fixture-only-0123456789012345678901234567890123456789".getBytes(StandardCharsets.UTF_8));
    static final String ISSUER="simulation-fixture-identity";
    static final UUID USER=UUID.fromString("00000000-0000-4000-8000-000000000001");
    static final JwtValidationProperties PROPS=new JwtValidationProperties(SECRET,ISSUER);
    static final String VERSION="dts-simulation-120-card01-v1";
    static SecretKey key(String context) {
        try {
            Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(Base64.getDecoder().decode(SECRET),"HmacSHA256"));
            return Keys.hmacShaKeyFor(mac.doFinal(context.getBytes(StandardCharsets.UTF_8)));
        } catch(Exception e) {throw new IllegalStateException(e);}
    }
    static String access(UUID user,String...permissions) {
        return Jwts.builder().issuer(ISSUER).subject(user.toString()).issuedAt(Date.from(Instant.now().minusSeconds(1)))
                .expiration(Date.from(Instant.now().plusSeconds(300))).claim("permissions",List.of(permissions))
                .claim("roles",List.of("ROLE_STUDENT")).signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET))).compact();
    }
    static SimulationBank.Published published() {
        var chapters=new ArrayList<SimulationBank.Chapter>();var scenarios=new ArrayList<SimulationBank.Scenario>();int number=0;
        int[] counts={29,14,20,10,17,30};
        for(int i=0;i<6;i++) {
            chapters.add(new SimulationBank.Chapter(i+1,"Chương "+(i+1),counts[i]));
            for(int j=0;j<counts[i];j++) {
                int n=++number;
                scenarios.add(new SimulationBank.Scenario(UUID.nameUUIDFromBytes(("scenario-"+n).getBytes(StandardCharsets.UTF_8)),n,i+1,"Tình huống "+n,60000,
                        UUID.nameUUIDFromBytes(("video-"+n).getBytes(StandardCharsets.UTF_8)),UUID.nameUUIDFromBytes(("image-"+n).getBytes(StandardCharsets.UTF_8)),
                        "Dấu hiệu "+n,"Mẹo "+n,new SimulationBank.Timeline(10000,11000,12000,13000,14000,15000),
                        new SimulationBank.Asset(n+".mp4","video/mp4","a".repeat(64),60000,60000),new SimulationBank.Asset(n+".webp","image/webp","b".repeat(64),null,null)));
            }
        }
        return new SimulationBank.Published(new SimulationBank.Snapshot(VERSION,"content-v1","assets-v2","scoring-v1","c".repeat(64),"d".repeat(64),"e".repeat(64),chapters,scenarios),"PUBLISHED",true,Instant.now());
    }
    static SimulationBank.Published copy(SimulationBank.Published p,String status,boolean current,SimulationBank.Snapshot s) {
        return new SimulationBank.Published(s,status,current,p.publishedAt());
    }
}
