package com.dts.practice.simulation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class SimulationPlaybackClient {
    private final URI origin;private final SimulationHttp http;private final SimulationTokens tokens;
    public SimulationPlaybackClient(@Value("${simulation.media-origin:http://localhost:8080}") String origin,SimulationHttp http,SimulationTokens tokens) {
        this.origin=SimulationHttp.origin(origin);this.http=http;this.tokens=tokens;
    }
    record Response(UUID mediaId,String url,OffsetDateTime expiredAt,String bankVersion) {}
    Response resolve(SimulationAccess.Actor actor,SimulationLearningModel.Row attempt) {
        var r=http.post(origin.resolve("/api/v1/media/simulation-playback"),Map.of("Authorization","Bearer "+actor.accessToken()),Map.of("ticket",tokens.playback(actor.userId(),attempt)),Response.class);
        try {
            var u=URI.create(r.url());var now=Instant.now();
            if(!attempt.snapshot.videoMediaId().equals(r.mediaId()) || !attempt.bank.equals(r.bankVersion()) || r.expiredAt()==null
                    || !r.expiredAt().toInstant().isAfter(now.plusSeconds(5)) || r.expiredAt().toInstant().isAfter(now.plusSeconds(310))
                    || !("http".equals(u.getScheme())||"https".equals(u.getScheme())) || u.getHost()==null || u.getUserInfo()!=null || u.getFragment()!=null || u.getQuery()==null) throw new IllegalArgumentException();
            return r;
        } catch(Exception e) {throw SimulationFailure.unavailable();}
    }
}
