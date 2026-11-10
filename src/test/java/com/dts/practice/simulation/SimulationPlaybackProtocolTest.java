package com.dts.practice.simulation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static com.dts.practice.simulation.SimulationLearningModel.*;

class SimulationPlaybackProtocolTest {
    @Test void nativeMediaTicketUsesRealOwnedAttemptAndSeparateSigningDomain() throws Exception {
        var mapper=new ObjectMapper().findAndRegisterModules();var bank=SimulationFixtures.published().snapshot();var row=new Row();
        row.id=UUID.randomUUID();row.user=SimulationFixtures.USER;row.bank=bank.bankVersion();row.snapshot=bank.scenarios().getFirst();
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);var captured=new AtomicReference<String>();
        server.createContext("/api/v1/media/simulation-playback",e->{try {
            captured.set(e.getRequestHeaders().getFirst("Authorization"));String token=mapper.readTree(e.getRequestBody()).get("ticket").asText();
            var c=Jwts.parser().verifyWith(SimulationFixtures.key("dts-media:simulation-playback:v1")).requireIssuer("dts-practice").require("purpose","SIMULATION_LEARNING").build().parseSignedClaims(token).getPayload();
            assertEquals(Set.of("dts-media"),c.getAudience());assertEquals(row.id.toString(),c.get("attemptId"));assertEquals(row.user.toString(),c.getSubject());
            assertEquals(row.snapshot.videoMediaId().toString(),c.get("mediaId"));assertEquals("SIMULATION_VIDEO",c.get("targetType"));assertEquals(row.bank,c.get("bankVersion"));
            assertTrue(c.getExpiration().getTime()-c.getIssuedAt().getTime()<=120000);
            assertThrows(Exception.class,()->Jwts.parser().verifyWith(SimulationFixtures.key("dts-media:simulation-catalog-thumbnail:v1")).build().parseSignedClaims(token));
            byte[] bytes=mapper.writeValueAsBytes(new SimulationPlaybackClient.Response(row.snapshot.videoMediaId(),"http://localhost:9000/fixture.mp4?signature=fixture",OffsetDateTime.now().plusSeconds(300),row.bank));
            e.getResponseHeaders().set("Content-Type","application/json");e.sendResponseHeaders(200,bytes.length);e.getResponseBody().write(bytes);
        }finally {e.close();}});server.start();
        try {
            var client=new SimulationPlaybackClient("http://127.0.0.1:"+server.getAddress().getPort(),new SimulationHttp(mapper),new SimulationTokens(SimulationFixtures.PROPS));
            assertNotNull(client.resolve(new SimulationAccess.Actor(row.user,"fixture-access"),row).url());assertEquals("Bearer fixture-access",captured.get());
        }finally {server.stop(0);}
    }
}
