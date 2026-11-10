package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SimulationProtocolTest {
    final SimulationTokens tokens=new SimulationTokens(SimulationFixtures.PROPS);
    final ObjectMapper json=new ObjectMapper().findAndRegisterModules();
    @Test void sourceTokenMatchesContentBuilderProtocolWithoutUserBearer() throws Exception {
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);var captured=new AtomicReference<io.jsonwebtoken.Claims>();
        server.createContext("/api/v1/content-builder/internal/simulation-banks/current",exchange->{
            try {
                assertNull(exchange.getRequestHeaders().getFirst("Authorization"));
                var signed=Jwts.parser().verifyWith(SimulationFixtures.key("dts:content-builder:simulation-source:v1")).requireIssuer("dts-practice")
                        .requireSubject("simulation-bank-reader").require("purpose","SIMULATION_SOURCE_READ").build()
                        .parseSignedClaims(exchange.getRequestHeaders().getFirst("X-DTS-Simulation-Source-Token"));
                assertEquals("HS256",signed.getHeader().getAlgorithm());assertEquals("dts-service+jwt",signed.getHeader().getType());
                assertEquals(Set.of("dts-content-builder"),signed.getPayload().getAudience());
                assertTrue(signed.getPayload().getExpiration().getTime()-signed.getPayload().getIssuedAt().getTime()<=60000);
                captured.set(signed.getPayload());byte[] bytes=json.writeValueAsBytes(SimulationFixtures.published());
                exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);
            } finally {exchange.close();}
        });
        server.start();
        try {
            var banks=new SimulationBankClient("http://127.0.0.1:"+server.getAddress().getPort(),new SimulationHttp(json),tokens);
            assertEquals(120,banks.current().scenarios().size());assertNotNull(captured.get());
        } finally {server.stop(0);}
    }
    @Test void catalogueTicketMatchesMediaProtocolAndIsNotAPlaybackOrAccessToken() throws Exception {
        var b=SimulationFixtures.published().snapshot();var scenario=b.scenarios().getFirst();String token=tokens.thumbnail(SimulationFixtures.USER,b,scenario);
        var signed=Jwts.parser().verifyWith(SimulationFixtures.key("dts-media:simulation-catalog-thumbnail:v1")).requireIssuer("dts-practice")
                .require("purpose","SIMULATION_CATALOG_THUMBNAIL").build().parseSignedClaims(token);var c=signed.getPayload();
        assertEquals("dts-simulation-catalog+jwt",signed.getHeader().getType());assertEquals("HS256",signed.getHeader().getAlgorithm());
        assertEquals(Set.of("dts-media"),c.getAudience());assertEquals(SimulationFixtures.USER.toString(),c.getSubject());
        assertEquals(scenario.scenarioId().toString(),c.get("scenarioId"));assertEquals(scenario.thumbnailMediaId().toString(),c.get("mediaId"));
        assertEquals("SIMULATION_THUMBNAIL",c.get("targetType"));assertEquals(b.bankVersion(),c.get("bankVersion"));
        assertNotNull(UUID.fromString(c.getId()));assertFalse(c.containsKey("attemptId"));
        assertTrue(c.getExpiration().getTime()-c.getIssuedAt().getTime()<=60000);
        assertThrows(Exception.class,()->Jwts.parser().verifyWith(SimulationFixtures.key("dts-media:simulation-playback:v1")).build().parseSignedClaims(token));
        assertThrows(BusinessException.class,()->tokens.access(token));
    }
    @Test void mediaReceivesSameLearnerBearerAndOnlySignedThumbnailBinding() throws Exception {
        var bank=SimulationFixtures.published().snapshot();var scenario=bank.scenarios().getFirst();String bearer=SimulationFixtures.access(SimulationFixtures.USER,"simulations:read","media:read");
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);var observed=new AtomicReference<String>();
        server.createContext("/api/v1/media/simulation-catalog-thumbnail",e->{try {
            observed.set(e.getRequestHeaders().getFirst("Authorization"));
            var body=json.readTree(e.getRequestBody());assertEquals(1,body.size());
            var c=Jwts.parser().verifyWith(SimulationFixtures.key("dts-media:simulation-catalog-thumbnail:v1")).build().parseSignedClaims(body.get("ticket").asText()).getPayload();
            assertEquals(scenario.thumbnailMediaId().toString(),c.get("mediaId"));
            byte[] bytes=json.writeValueAsBytes(new SimulationThumbnailClient.Thumbnail(scenario.thumbnailMediaId(),scenario.scenarioId(),bank.bankVersion(),"http://localhost:9000/fixture/1.webp?signature=fixture",OffsetDateTime.now().plusSeconds(300)));
            e.getResponseHeaders().set("Content-Type","application/json");e.sendResponseHeaders(200,bytes.length);e.getResponseBody().write(bytes);
        } finally {e.close();}});server.start();
        try {
            var client=new SimulationThumbnailClient("http://127.0.0.1:"+server.getAddress().getPort(),new SimulationHttp(json),tokens);
            assertEquals(scenario.scenarioId(),client.resolve(new SimulationAccess.Actor(SimulationFixtures.USER,bearer),bank,scenario).scenarioId());
            assertEquals("Bearer "+bearer,observed.get());
        } finally {server.stop(0);}
    }
    @Test void redirectsAreRejectedWithoutFollowingTheLocation() throws Exception {
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);var followed=new java.util.concurrent.atomic.AtomicBoolean();
        server.createContext("/redirect",e->{e.getResponseHeaders().set("Location","/leak");e.sendResponseHeaders(302,-1);e.close();});
        server.createContext("/leak",e->{followed.set(true);e.sendResponseHeaders(200,-1);e.close();});server.start();
        try {
            var http=new SimulationHttp(json);var url=java.net.URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/redirect");
            assertThrows(BusinessException.class,()->http.get(url,Map.of("Authorization","Bearer fixture"),Object.class,false));assertFalse(followed.get());
        } finally {server.stop(0);}
    }
    @Test void configuredOriginCannotCarryCredentialsPathOrQuery() {
        for(String value:List.of("file:///tmp","https://user:password@example.invalid","http://localhost:8080/path","http://localhost:8080?override=true"))
            assertThrows(IllegalArgumentException.class,()->SimulationHttp.origin(value));
    }
    @ParameterizedTest @ValueSource(strings={"media","scenario","bank","expired","too-long","unsigned","scheme","credentials","fragment"})
    void wrongMediaResponseBindingOrUrlIsRejected(String kind) {
        var bank=SimulationFixtures.published().snapshot();var scenario=bank.scenarios().getFirst();
        UUID media=scenario.thumbnailMediaId(),id=scenario.scenarioId();String version=bank.bankVersion();
        String url="http://localhost:9000/fixture/1.webp?signature=fixture";var expiry=OffsetDateTime.now().plusSeconds(300);
        switch(kind) {
            case "media" -> media=UUID.randomUUID();
            case "scenario" -> id=UUID.randomUUID();
            case "bank" -> version="other-bank";
            case "expired" -> expiry=OffsetDateTime.now().minusSeconds(1);
            case "too-long" -> expiry=OffsetDateTime.now().plusHours(1);
            case "unsigned" -> url="http://localhost:9000/fixture/1.webp";
            case "scheme" -> url="javascript:alert(1)";
            case "credentials" -> url="http://user:password@localhost:9000/fixture/1.webp?signature=fixture";
            case "fragment" -> url += "#fragment";
        }
        var http=mock(SimulationHttp.class);
        when(http.post(any(),anyMap(),any(),eq(SimulationThumbnailClient.Thumbnail.class)))
                .thenReturn(new SimulationThumbnailClient.Thumbnail(media,id,version,url,expiry));
        var client=new SimulationThumbnailClient("http://localhost:8080",http,tokens);
        assertEquals(503,assertThrows(BusinessException.class,()->client.resolve(new SimulationAccess.Actor(SimulationFixtures.USER,"fixture"),bank,scenario)).getStatus().value());
    }
    @Test void oversizedDependencyResponseIsRejected() throws Exception {
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/large",e->{try {
            byte[] bytes=new byte[2*1024*1024+1];Arrays.fill(bytes,(byte)' ');e.getResponseHeaders().set("Content-Type","application/json");
            e.sendResponseHeaders(200,bytes.length);e.getResponseBody().write(bytes);
        } finally {e.close();}});server.start();
        try {
            assertThrows(BusinessException.class,()->new SimulationHttp(json).get(java.net.URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/large"),Map.of(),Object.class,false));
        } finally {server.stop(0);}
    }
}
