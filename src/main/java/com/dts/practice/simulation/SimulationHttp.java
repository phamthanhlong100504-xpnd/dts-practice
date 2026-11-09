package com.dts.practice.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

/** Bounded, non-redirecting HTTP calls only to configured service origins. */
@Component
public class SimulationHttp {
    private static final int LIMIT=2*1024*1024;
    private final ObjectMapper json;
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NEVER).proxy(new ProxySelector() {
                @Override public List<Proxy> select(URI uri) {return List.of(Proxy.NO_PROXY);}
                @Override public void connectFailed(URI uri,SocketAddress address,IOException e) {}
            }).build();
    public SimulationHttp(ObjectMapper json) {this.json=json;}
    static URI origin(String value) {
        try {
            URI uri=URI.create(value);
            if(!List.of("http","https").contains(uri.getScheme()) || uri.getHost()==null || uri.getRawUserInfo()!=null
                    || uri.getRawQuery()!=null || uri.getRawFragment()!=null || !(uri.getRawPath().isEmpty() || "/".equals(uri.getRawPath())))
                throw new IllegalArgumentException();
            return uri.resolve("/");
        } catch(Exception e) {throw new IllegalArgumentException("Invalid simulation service origin");}
    }
    <T> T get(URI url,Map<String,String> headers,Class<T> type,boolean missingIsNotFound) {
        return send(url,headers,null,type,missingIsNotFound);
    }
    <T> T post(URI url,Map<String,String> headers,Object body,Class<T> type) {return send(url,headers,body,type,false);}
    private <T> T send(URI url,Map<String,String> headers,Object body,Class<T> type,boolean missingIsNotFound) {
        try {
            var request=HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(8)).header("Accept","application/json");
            headers.forEach(request::header);
            if(body==null) request.GET();else request.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofByteArray(json.writeValueAsBytes(body)));
            // Bound the complete response, including a dependency that stalls its body.
            var pending=client.sendAsync(request.build(),ignored->new LimitedBody());
            HttpResponse<byte[]> response;
            try {response=pending.get(8,TimeUnit.SECONDS);}
            finally {if(!pending.isDone()) pending.cancel(true);}
            if(response.statusCode()==404 && missingIsNotFound) throw SimulationFailure.notFound();
            if(response.statusCode()!=200 || !response.headers().firstValue("Content-Type").orElse("").startsWith("application/json")) throw SimulationFailure.unavailable();
            return json.readValue(response.body(),type);
        } catch(com.dts.practice.exception.BusinessException e) {throw e;}
        catch(InterruptedException e) {Thread.currentThread().interrupt();throw SimulationFailure.unavailable();}
        catch(Exception e) {throw SimulationFailure.unavailable();}
    }
    private static final class LimitedBody implements HttpResponse.BodySubscriber<byte[]> {
        private final HttpResponse.BodySubscriber<byte[]> delegate=HttpResponse.BodySubscribers.ofByteArray();
        private Flow.Subscription subscription;
        private long bytes;
        private boolean failed;
        @Override public CompletionStage<byte[]> getBody() {return delegate.getBody();}
        @Override public void onSubscribe(Flow.Subscription value) {subscription=value;delegate.onSubscribe(value);}
        @Override public void onNext(List<ByteBuffer> chunks) {
            if(failed) return;
            for(var chunk:chunks) bytes+=chunk.remaining();
            if(bytes>LIMIT) {failed=true;subscription.cancel();delegate.onError(new IOException("Simulation response limit exceeded"));}
            else delegate.onNext(chunks);
        }
        @Override public void onError(Throwable error) {delegate.onError(error);}
        @Override public void onComplete() {if(!failed) delegate.onComplete();}
    }
}
