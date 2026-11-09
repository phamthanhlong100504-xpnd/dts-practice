package com.dts.practice.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@Component
public class SimulationThumbnailClient {
    private final URI origin;
    private final SimulationHttp http;
    private final SimulationTokens tokens;
    public SimulationThumbnailClient(@Value("${simulation.media-origin:http://localhost:8080}") String origin,SimulationHttp http,SimulationTokens tokens) {
        this.origin=SimulationHttp.origin(origin);this.http=http;this.tokens=tokens;
    }
    Thumbnail resolve(SimulationAccess.Actor actor,SimulationBank.Snapshot bank,SimulationBank.Scenario scenario) {
        var response=http.post(origin.resolve("/api/v1/media/simulation-catalog-thumbnail"),Map.of("Authorization","Bearer "+actor.accessToken()),
                Map.of("ticket",tokens.thumbnail(actor.userId(),bank,scenario)),Thumbnail.class);
        try {
            var url=URI.create(response.url());var now=Instant.now();
            if(!scenario.thumbnailMediaId().equals(response.mediaId()) || !scenario.scenarioId().equals(response.scenarioId())
                    || !bank.bankVersion().equals(response.bankVersion()) || response.expiredAt()==null
                    || !response.expiredAt().toInstant().isAfter(now.plusSeconds(5)) || response.expiredAt().toInstant().isAfter(now.plusSeconds(310))
                    || !List.of("http","https").contains(url.getScheme()) || url.getHost()==null || url.getRawUserInfo()!=null || url.getFragment()!=null
                    || url.getRawQuery()==null) throw new IllegalArgumentException();
            return response;
        } catch(Exception e) {throw SimulationFailure.unavailable();}
    }
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Thumbnail(UUID mediaId,UUID scenarioId,String bankVersion,String url,OffsetDateTime expiredAt) {}
}
