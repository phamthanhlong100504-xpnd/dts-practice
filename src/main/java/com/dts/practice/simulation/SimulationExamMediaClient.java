package com.dts.practice.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.*;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Flow;

@Component
public class SimulationExamMediaClient implements AutoCloseable {
    public record Eligible(UUID mediaId,boolean ready,int durationMs,int videoTrackDurationMs,String objectEtag,String reason) {}
    public record Eligibility(String bankVersion,String manifestSha256,List<Eligible> items) {}
    public record Video(UUID sessionId,int position,UUID playbackEventId,String bankVersion,String manifestSha256,UUID mediaId,String url,OffsetDateTime expiredAt,String mimeType,int durationMs,String usage) {}
    private final URI origin;private final SimulationExamTickets tickets;private final ObjectMapper json;private final HttpClient client;
    public SimulationExamMediaClient(@Value("${simulation.media-origin:http://localhost:8080}")String origin,SimulationExamTickets tickets,ObjectMapper json) {
        this.origin=SimulationHttp.origin(origin);this.tickets=tickets;this.json=json;
        this.client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).proxy(new ProxySelector(){
            @Override public List<Proxy> select(URI uri){return List.of(Proxy.NO_PROXY);}@Override public void connectFailed(URI u,SocketAddress a,java.io.IOException e){}
        }).build();
    }
    List<Eligible> eligibility(SimulationAccess.Actor actor,SimulationBank.Snapshot bank,List<SimulationBank.Scenario> candidates) {
        var response=post("eligibility",actor,tickets.eligibility(actor.userId(),bank,candidates),Eligibility.class);
        if(response==null||!bank.bankVersion().equals(response.bankVersion())||!bank.manifestSha256().equals(response.manifestSha256())||response.items()==null||response.items().size()!=candidates.size())throw SimulationExamFailure.unavailable();
        for(int i=0;i<candidates.size();i++) {
            var item=response.items().get(i);var s=candidates.get(i);
            if(item==null||!s.videoMediaId().equals(item.mediaId())||s.durationMs()!=item.durationMs()||s.video().videoTrackDurationMs()!=item.videoTrackDurationMs()
                    ||item.reason()==null||!item.reason().matches("[A-Z_]{1,40}")
                    ||(item.ready()&&(!"READY".equals(item.reason())||item.objectEtag()==null||!item.objectEtag().matches("[A-Za-z0-9_-]{1,128}")))
                    ||(!item.ready()&&(item.objectEtag()!=null||"READY".equals(item.reason()))))throw SimulationExamFailure.unavailable();
        }
        return List.copyOf(response.items());
    }
    Video playback(SimulationAccess.Actor actor,SimulationExamModel.Row row,SimulationExamModel.Frame frame,boolean review) {
        var v=post(review?"review-playback":"playback",actor,tickets.playback(actor.userId(),row,frame,review),Video.class);
        try {
            URI url=URI.create(v.url());Instant now=Instant.now();
            if(!row.id.equals(v.sessionId())||frame.position!=v.position()||!frame.playbackId.equals(v.playbackEventId())||!row.bank.equals(v.bankVersion())
                    ||!row.manifest.equals(v.manifestSha256())||!frame.snapshot.videoMediaId().equals(v.mediaId())||!"video/mp4".equals(v.mimeType())||frame.duration!=v.durationMs()
                    ||!(review?"REVIEW":"CURRENT").equals(v.usage())||v.expiredAt()==null||!v.expiredAt().toInstant().isAfter(now.plusSeconds(5))||v.expiredAt().toInstant().isAfter(now.plusSeconds(310))
                    ||!List.of("http","https").contains(url.getScheme())||url.getHost()==null||url.getUserInfo()!=null||url.getFragment()!=null||url.getRawQuery()==null)throw new IllegalArgumentException();
            return v;
        }catch(Exception e){throw SimulationExamFailure.unavailable();}
    }
    private<T>T post(String operation,SimulationAccess.Actor actor,String ticket,Class<T> type) {
        try {
            var request=HttpRequest.newBuilder(origin.resolve("/api/v1/media/simulation-exam/"+operation)).timeout(Duration.ofSeconds(45))
                    .header("Authorization","Bearer "+actor.accessToken()).header("Accept","application/json").header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(json.writeValueAsBytes(Map.of("ticket",ticket)))).build();
            var pending=client.sendAsync(request,ignored->new Bounded());HttpResponse<byte[]> response;
            try{response=pending.get(45,TimeUnit.SECONDS);}finally{if(!pending.isDone())pending.cancel(true);}
            if(response.statusCode()==409)throw SimulationExamFailure.conflict("SIMULATION_EXAM_MEDIA_CHANGED","The pinned exam video is unavailable or changed");
            if(response.statusCode()!=200||!response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"))throw SimulationExamFailure.unavailable();
            return json.readValue(response.body(),type);
        }catch(SimulationExamFailure e){throw e;}catch(InterruptedException e){Thread.currentThread().interrupt();throw SimulationExamFailure.unavailable();}catch(Exception e){throw SimulationExamFailure.unavailable();}
    }
    private static final class Bounded implements HttpResponse.BodySubscriber<byte[]> {
        private final HttpResponse.BodySubscriber<byte[]> delegate=HttpResponse.BodySubscribers.ofByteArray();private Flow.Subscription subscription;private long size;private boolean failed;
        @Override public CompletionStage<byte[]>getBody(){return delegate.getBody();}@Override public void onSubscribe(Flow.Subscription s){subscription=s;delegate.onSubscribe(s);}
        @Override public void onNext(List<ByteBuffer> data){if(failed)return;for(var b:data)size+=b.remaining();if(size>1024*1024){failed=true;subscription.cancel();delegate.onError(new java.io.IOException("Media response too large"));}else delegate.onNext(data);}
        @Override public void onError(Throwable e){delegate.onError(e);}@Override public void onComplete(){if(!failed)delegate.onComplete();}
    }
    @Override public void close(){client.close();}
}
