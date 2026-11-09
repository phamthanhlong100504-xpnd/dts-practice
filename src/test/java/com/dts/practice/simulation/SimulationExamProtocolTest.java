package com.dts.practice.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.net.InetSocketAddress;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static com.dts.practice.simulation.SimulationExamModel.*;

class SimulationExamProtocolTest {
    final ObjectMapper json=new ObjectMapper().findAndRegisterModules();
    final SimulationExamTickets tickets=new SimulationExamTickets(SimulationFixtures.PROPS);
    final SimulationBank.Snapshot bank=SimulationFixtures.published().snapshot();
    Row row(){var r=new Row();r.id=UUID.randomUUID();r.user=SimulationFixtures.USER;r.bank=bank.bankVersion();r.manifest=bank.manifestSha256();return r;}
    Frame frame(Row r){var f=new Frame();f.session=r.id;f.position=1;f.playbackId=UUID.randomUUID();f.snapshot=bank.scenarios().getFirst();f.duration=f.snapshot.durationMs();f.etag="fixtureetag";return f;}
    @ParameterizedTest @ValueSource(strings={"eligibility","playback","review"}) void exactMediaV2TicketDomainAndClaims(String kind) {
        var r=row();var f=frame(r);String token=kind.equals("eligibility")?tickets.eligibility(r.user,bank,List.of(f.snapshot)):tickets.playback(r.user,r,f,kind.equals("review"));
        var signed=Jwts.parser().verifyWith(SimulationFixtures.key("dts-media:simulation-exam-"+kind+":v1")).build().parseSignedClaims(token);var c=signed.getPayload();
        assertEquals("HS256",signed.getHeader().getAlgorithm());assertEquals("dts-simulation-exam-"+kind+"+jwt",signed.getHeader().getType());assertEquals("dts-practice",c.getIssuer());
        assertEquals(Set.of("dts-media"),c.getAudience());assertEquals(r.user.toString(),c.getSubject());assertEquals("SIMULATION_EXAM_"+kind.toUpperCase(Locale.ROOT),c.get("purpose"));
        Set<String> expected=new HashSet<>(Set.of("iss","aud","sub","iat","exp","jti","purpose","bankVersion","manifestSha256","targetType"));
        expected.addAll(kind.equals("eligibility")?Set.of("candidates"):Set.of("sessionId","position","playbackEventId","asset","objectEtag"));assertEquals(expected,c.keySet());
        Object asset=kind.equals("eligibility")?((List<?>)c.get("candidates")).getFirst():c.get("asset");assertEquals(Set.of("mediaId","fileName","sha256","durationMs","videoTrackDurationMs"),((Map<?,?>)asset).keySet());
        assertEquals(bank.bankVersion(),c.get("bankVersion"));assertEquals(bank.manifestSha256(),c.get("manifestSha256"));assertEquals("SIMULATION_VIDEO",c.get("targetType"));
        assertEquals(45000,c.getExpiration().getTime()-c.getIssuedAt().getTime());assertNotNull(UUID.fromString(c.getId()));assertTrue(token.length()<16384);
        for(String other:List.of("eligibility","playback","review"))if(!other.equals(kind))assertThrows(Exception.class,()->Jwts.parser().verifyWith(SimulationFixtures.key("dts-media:simulation-exam-"+other+":v1")).build().parseSignedClaims(token));
        assertThrows(Exception.class,()->Jwts.parser().verifyWith(SimulationFixtures.key("dts-media:simulation-playback:v1")).build().parseSignedClaims(token));
        assertThrows(Exception.class,()->new SimulationTokens(SimulationFixtures.PROPS).access(token));
    }
    @Test void emptyAndOversizedEligibilityBatchesCannotMintTicket() {
        assertThrows(IllegalArgumentException.class,()->tickets.eligibility(SimulationFixtures.USER,bank,List.of()));
        assertThrows(IllegalArgumentException.class,()->tickets.eligibility(SimulationFixtures.USER,bank,bank.scenarios().subList(0,17)));
    }
    @Test void mediaReceivesOnlyPrivateTicketAndOriginalActorBearer()throws Exception {
        var r=row();var f=frame(r);var observed=new AtomicReference<String>();var failure=new AtomicReference<Throwable>();
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/api/v1/media/simulation-exam/eligibility",e->{try {
            observed.set(e.getRequestHeaders().getFirst("Authorization"));var body=json.readTree(e.getRequestBody());assertEquals(Set.of("ticket"),json.convertValue(body,Map.class).keySet());
            var c=Jwts.parser().verifyWith(SimulationFixtures.key("dts-media:simulation-exam-eligibility:v1")).build().parseSignedClaims(body.path("ticket").asText()).getPayload();assertEquals(r.user.toString(),c.getSubject());
            byte[] bytes=json.writeValueAsBytes(new SimulationExamMediaClient.Eligibility(r.bank,r.manifest,List.of(new SimulationExamMediaClient.Eligible(f.snapshot.videoMediaId(),true,f.duration,f.duration,"fixtureetag","READY"))));
            e.getResponseHeaders().set("Content-Type","application/json");e.sendResponseHeaders(200,bytes.length);e.getResponseBody().write(bytes);
        }catch(Throwable t){failure.set(t);e.sendResponseHeaders(500,-1);}finally{e.close();}});server.start();
        try(var client=new SimulationExamMediaClient("http://127.0.0.1:"+server.getAddress().getPort(),tickets,json)) {
            assertTrue(client.eligibility(new SimulationAccess.Actor(r.user,"fixture-bearer"),bank,List.of(f.snapshot)).getFirst().ready());assertNull(failure.get());assertEquals("Bearer fixture-bearer",observed.get());
        }finally{server.stop(0);}
    }
    @ParameterizedTest @ValueSource(strings={"session","position","nonce","bank","manifest","media","mime","duration","usage","expired","too-long","unsigned","credentials","fragment","scheme"})
    void wrongPlaybackBindingCannotEscapeToFrontend(String bad)throws Exception {
        var r=row();var f=frame(r);var node=json.valueToTree(new SimulationExamMediaClient.Video(r.id,1,f.playbackId,r.bank,r.manifest,f.snapshot.videoMediaId(),"http://localhost:9000/media/fixture.mp4?signature=fixture",OffsetDateTime.now().plusSeconds(300),"video/mp4",f.duration,"CURRENT"));
        var body=(ObjectNode)node;
        switch(bad) {
            case "session"->body.put("sessionId",UUID.randomUUID().toString());case "position"->body.put("position",2);case "nonce"->body.put("playbackEventId",UUID.randomUUID().toString());
            case "bank"->body.put("bankVersion","other");case "manifest"->body.put("manifestSha256","a".repeat(64));case "media"->body.put("mediaId",UUID.randomUUID().toString());
            case "mime"->body.put("mimeType","text/html");case "duration"->body.put("durationMs",1);case "usage"->body.put("usage","REVIEW");
            case "expired"->body.put("expiredAt",OffsetDateTime.now().minusSeconds(1).toString());case "too-long"->body.put("expiredAt",OffsetDateTime.now().plusHours(1).toString());
            case "unsigned"->body.put("url","http://localhost:9000/media/fixture.mp4");case "credentials"->body.put("url","http://u:p@localhost:9000/media/f.mp4?s=x");
            case "fragment"->body.put("url","http://localhost:9000/media/f.mp4?s=x#bad");case "scheme"->body.put("url","file:///tmp/private?s=x");
        }
        withResponse(200,json.writeValueAsBytes(body),client->assertThrows(SimulationExamFailure.class,()->client.playback(new SimulationAccess.Actor(r.user,"fixture"),r,f,false)));
    }
    @ParameterizedTest @ValueSource(ints={302,401,403,404,409,500,503}) void mediaRejectionsFailClosed(int status)throws Exception {
        var r=row();var f=frame(r);withResponse(status,"{}".getBytes(java.nio.charset.StandardCharsets.UTF_8),client->{var e=assertThrows(SimulationExamFailure.class,()->client.playback(new SimulationAccess.Actor(r.user,"fixture"),r,f,false));assertEquals(status==409?409:503,e.status.value());});
    }
    @ParameterizedTest @ValueSource(strings={"order","bank","duration","etag","ready-reason","unready-etag","count"}) void inconsistentEligibilityRejected(String kind)throws Exception {
        var s=bank.scenarios().getFirst();var item=new SimulationExamMediaClient.Eligible(s.videoMediaId(),true,s.durationMs(),s.durationMs(),"fixtureetag","READY");
        var body=(ObjectNode)json.valueToTree(new SimulationExamMediaClient.Eligibility(bank.bankVersion(),bank.manifestSha256(),List.of(item)));var first=(ObjectNode)body.path("items").get(0);
        switch(kind){case "order"->first.put("mediaId",UUID.randomUUID().toString());case "bank"->body.put("bankVersion","other");case "duration"->first.put("durationMs",1);case "etag"->first.putNull("objectEtag");case "ready-reason"->first.put("reason","NOT_READY");case "unready-etag"->first.put("ready",false);case "count"->body.putArray("items");}
        withResponse(200,json.writeValueAsBytes(body),client->assertThrows(SimulationExamFailure.class,()->client.eligibility(new SimulationAccess.Actor(SimulationFixtures.USER,"fixture"),bank,List.of(s))));
    }
    @Test void oversizedMediaResponseIsBounded()throws Exception {withResponse(200,new byte[1024*1024+1],client->assertThrows(SimulationExamFailure.class,()->client.eligibility(new SimulationAccess.Actor(SimulationFixtures.USER,"fixture"),bank,List.of(bank.scenarios().getFirst()))));}
    @ParameterizedTest @ValueSource(booleans={false,true}) void matchingCurrentAndReviewResponsesAreAccepted(boolean review)throws Exception {
        var r=row();var f=frame(r);var bytes=json.writeValueAsBytes(new SimulationExamMediaClient.Video(r.id,1,f.playbackId,r.bank,r.manifest,f.snapshot.videoMediaId(),"http://localhost:9000/media/fixture.mp4?signature=fixture",OffsetDateTime.now().plusSeconds(300),"video/mp4",f.duration,review?"REVIEW":"CURRENT"));
        withResponse(200,bytes,client->{var video=client.playback(new SimulationAccess.Actor(r.user,"fixture"),r,f,review);assertEquals(review?"REVIEW":"CURRENT",video.usage());assertEquals(f.playbackId,video.playbackEventId());});
    }
    void withResponse(int status,byte[] bytes,java.util.function.Consumer<SimulationExamMediaClient> action)throws Exception {
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);server.createContext("/",e->{try{e.getRequestBody().readAllBytes();e.getResponseHeaders().set("Content-Type","application/json");e.getResponseHeaders().set("Location","/unexpected");e.sendResponseHeaders(status,bytes.length);e.getResponseBody().write(bytes);}catch(java.io.IOException ignored){}finally{e.close();}});server.start();
        try(var client=new SimulationExamMediaClient("http://127.0.0.1:"+server.getAddress().getPort(),tickets,json)){action.accept(client);}finally{server.stop(0);}
    }
}
