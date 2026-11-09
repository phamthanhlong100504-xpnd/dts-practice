package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.net.InetSocketAddress;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SimulationReplayProtocolTest {
    @ParameterizedTest @ValueSource(strings={"valid","expired","wrong-bank","wrong-media"})
    void replayUsesExistingMediaProtocolWithRealOwnedCompletedAttempt(String responseKind) throws Exception {
        var mapper=new ObjectMapper().findAndRegisterModules();var bank=SimulationFixtures.published().snapshot();
        var row=new SimulationLearningModel.Row();row.id=UUID.randomUUID();row.user=SimulationFixtures.USER;
        row.bank=bank.bankVersion();row.scoring=bank.scoringVersion();row.manifest=bank.manifestSha256();
        row.snapshot=bank.scenarios().getFirst();row.scenarioId=row.snapshot.scenarioId();row.duration=row.snapshot.durationMs();row.status="COMPLETED";
        var access=mock(SimulationAccess.class);var repo=mock(SimulationLearningRepository.class);var banks=mock(SimulationBankClient.class);
        when(access.read(true)).thenReturn(new SimulationAccess.Actor(row.user,"fixture-access"));
        when(repo.get(row.user,row.id,false)).thenReturn(row);when(banks.load(row.bank)).thenReturn(bank);
        var observed=new AtomicReference<Throwable>();
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/api/v1/media/simulation-playback",exchange->{
            try {
                assertEquals("POST",exchange.getRequestMethod());assertEquals("Bearer fixture-access",exchange.getRequestHeaders().getFirst("Authorization"));
                var token=mapper.readTree(exchange.getRequestBody()).get("ticket").asText();
                var claims=Jwts.parser().verifyWith(SimulationFixtures.key("dts-media:simulation-playback:v1"))
                        .requireIssuer("dts-practice").require("purpose","SIMULATION_LEARNING").build().parseSignedClaims(token).getPayload();
                assertEquals(row.id.toString(),claims.get("attemptId"));assertEquals(row.user.toString(),claims.getSubject());
                assertEquals(row.snapshot.videoMediaId().toString(),claims.get("mediaId"));assertEquals(row.bank,claims.get("bankVersion"));
                assertEquals(Set.of("dts-media"),claims.getAudience());assertEquals("SIMULATION_VIDEO",claims.get("targetType"));
                var response=new SimulationPlaybackClient.Response(responseKind.equals("wrong-media")?UUID.randomUUID():row.snapshot.videoMediaId(),
                        "http://localhost/private-replay.mp4?signature=fixture",OffsetDateTime.now().plusSeconds(responseKind.equals("expired")?-1:300),
                        responseKind.equals("wrong-bank")?"other-bank":row.bank);
                byte[] bytes=mapper.writeValueAsBytes(response);exchange.getResponseHeaders().set("Content-Type","application/json");
                exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);
            } catch(Throwable failure) {observed.set(failure);exchange.sendResponseHeaders(403,-1);}
            finally {exchange.close();}
        });server.start();
        try {
            var client=new SimulationPlaybackClient("http://127.0.0.1:"+server.getAddress().getPort(),new SimulationHttp(mapper),new SimulationTokens(SimulationFixtures.PROPS));
            var service=new SimulationReplayService(access,repo,banks,client);
            if(responseKind.equals("valid")) {
                var replay=service.replay(row.id);assertTrue(replay.readOnly());assertEquals(row.id,replay.attemptId());assertEquals(row.scenarioId,replay.scenarioId());
            } else assertEquals("SIMULATION_DEPENDENCY_UNAVAILABLE",assertThrows(BusinessException.class,()->service.replay(row.id)).getErrorCode());
            assertNull(observed.get());verify(repo).get(row.user,row.id,false);verifyNoMoreInteractions(repo);verify(access,never()).learn();
        } finally {server.stop(0);}
    }
}
