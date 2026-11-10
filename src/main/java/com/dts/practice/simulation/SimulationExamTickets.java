package com.dts.practice.simulation;

import com.dts.practice.config.JwtValidationProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/** Media v1 signing contract, matching accepted Card05 Media kit2. Never return tickets to clients. */
@Component
public class SimulationExamTickets {
    private final Map<String,SecretKey> keys;
    public SimulationExamTickets(JwtValidationProperties properties) {
        try {
            String secret=properties.accessSecret();byte[] root;
            try{root=Decoders.BASE64.decode(secret);}catch(Exception e){try{root=Decoders.BASE64URL.decode(secret);}catch(Exception ignored){root=secret.getBytes(StandardCharsets.UTF_8);}}
            if(root.length<32)root=MessageDigest.getInstance("SHA-256").digest(root);Map<String,SecretKey> map=new HashMap<>();
            for(String kind:List.of("eligibility","playback","review")){Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(root,"HmacSHA256"));map.put(kind,Keys.hmacShaKeyFor(mac.doFinal(("dts-media:simulation-exam-"+kind+":v1").getBytes(StandardCharsets.UTF_8))));}
            keys=Map.copyOf(map);
        }catch(Exception e){throw new IllegalStateException("Invalid simulation exam signing configuration");}
    }
    public record Asset(UUID mediaId,String fileName,String sha256,int durationMs,int videoTrackDurationMs) {}
    static Asset asset(SimulationBank.Scenario s){return new Asset(s.videoMediaId(),s.video().fileName(),s.video().sha256(),s.durationMs(),s.video().videoTrackDurationMs());}
    String eligibility(UUID user,SimulationBank.Snapshot bank,List<SimulationBank.Scenario> candidates) {
        if(candidates.isEmpty()||candidates.size()>16)throw new IllegalArgumentException("Eligibility batch1..16 required");
        return mint("eligibility",user,bank.bankVersion(),bank.manifestSha256(),Map.of("candidates",candidates.stream().map(SimulationExamTickets::asset).toList()));
    }
    String playback(UUID user,SimulationExamModel.Row row,SimulationExamModel.Frame frame,boolean review) {
        return mint(review?"review":"playback",user,row.bank,row.manifest,Map.of("sessionId",row.id.toString(),"position",frame.position,"playbackEventId",frame.playbackId.toString(),"asset",asset(frame.snapshot),"objectEtag",frame.etag));
    }
    private String mint(String kind,UUID user,String bank,String manifest,Map<String,Object> extra) {
        var now=Instant.now();var b=Jwts.builder().header().type("dts-simulation-exam-"+kind+"+jwt").and().issuer("dts-practice").subject(user.toString())
                .audience().add("dts-media").and().issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(45))).id(UUID.randomUUID().toString())
                .claim("purpose","SIMULATION_EXAM_"+kind.toUpperCase(Locale.ROOT)).claim("bankVersion",bank).claim("manifestSha256",manifest).claim("targetType","SIMULATION_VIDEO");
        extra.forEach(b::claim);String token=b.signWith(keys.get(kind),Jwts.SIG.HS256).compact();
        if(token.length()>16384)throw SimulationExamFailure.unavailable();return token;
    }
}
