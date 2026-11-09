package com.dts.practice.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dts.practice.exception.BusinessException;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class SimulationPresetSourceClientTest {
    HttpServer server;SimulationBankClient client;final ObjectMapper json=new ObjectMapper().findAndRegisterModules();
    final SimulationBank.Snapshot bank=SimulationFixtures.published().snapshot();final AtomicReference<String> mode=new AtomicReference<>("normal"),header=new AtomicReference<>(),authorization=new AtomicReference<>(),requestedPath=new AtomicReference<>();
    @BeforeEach void start()throws Exception {
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);server.createContext("/",exchange->{
            header.set(exchange.getRequestHeaders().getFirst("X-DTS-Simulation-Source-Token"));authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));String path=exchange.getRequestURI().getPath();requestedPath.set(path);
            int status=200;Object value;
            if(mode.get().equals("error")){status=500;value=Map.of();}
            else if(mode.get().equals("redirect")){status=302;exchange.getResponseHeaders().set("Location","http://127.0.0.1:"+server.getAddress().getPort()+"/redirect-trap");value=Map.of();}
            else if(path.endsWith("/current"))value=SimulationPresetFixtures.catalogue(bank);
            else if(path.endsWith(SimulationPresetFixtures.version(bank,1,"v1").presetVersionId().toString()))value=SimulationPresetFixtures.version(bank,1,"v1");
            else{status=404;value=Map.of();}
            byte[] bytes=mode.get().equals("oversize")?new byte[2*1024*1024+1024]:json.writeValueAsBytes(value);exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(status,bytes.length);
            try(var body=exchange.getResponseBody()){body.write(bytes);}catch(java.io.IOException ignored){}finally{exchange.close();}
        });server.start();client=new SimulationBankClient("http://127.0.0.1:"+server.getAddress().getPort(),new SimulationHttp(json),new SimulationTokens(SimulationFixtures.PROPS));
    }
    @AfterEach void stop(){server.stop(0);}
    @Test void currentSourceUsesTheExistingDedicatedServiceTokenWithoutLearnerCredentials(){var c=SimulationPresets.validate(client.presets(),bank);assertEquals(15,c.items().size());assertNull(authorization.get());var token=Jwts.parser().verifyWith(SimulationFixtures.key("dts:content-builder:simulation-source:v1")).requireIssuer("dts-practice").requireSubject("simulation-bank-reader").require("purpose","SIMULATION_SOURCE_READ").build().parseSignedClaims(header.get());assertEquals("dts-service+jwt",token.getHeader().getType());assertTrue(token.getPayload().getAudience().contains("dts-content-builder"));assertTrue(token.getPayload().getExpiration().getTime()-token.getPayload().getIssuedAt().getTime()<=60000);assertTrue(requestedPath.get().endsWith("/simulation-presets/current"));}
    @Test void pinnedVersionUsesCanonicalPathAndExactOrder(){var expected=SimulationPresetFixtures.version(bank,1,"v1");var value=SimulationPresets.validate(client.preset(expected.presetVersionId()),bank);assertEquals(expected,value);assertTrue(requestedPath.get().endsWith("/"+expected.presetVersionId()));}
    @Test void missingVersionIs404(){assertEquals(404,assertThrows(BusinessException.class,()->client.preset(UUID.randomUUID())).getStatus().value());}
    @Test void upstreamFailureIsUnavailable(){mode.set("error");assertEquals(503,assertThrows(BusinessException.class,client::presets).getStatus().value());}
    @Test void redirectsAreNotFollowed(){mode.set("redirect");assertEquals(503,assertThrows(BusinessException.class,client::presets).getStatus().value());assertFalse(requestedPath.get().contains("redirect-trap"));}
    @Test void oversizedResponsesAreBounded(){mode.set("oversize");assertEquals(503,assertThrows(BusinessException.class,client::presets).getStatus().value());}
}
