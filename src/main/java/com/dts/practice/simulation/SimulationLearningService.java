package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import static com.dts.practice.simulation.SimulationLearningModel.*;
import static com.dts.practice.simulation.SimulationLearningRules.*;

@Service
@RequiredArgsConstructor
public class SimulationLearningService {
    private final SimulationAccess access;
    private final SimulationBankClient banks;
    private final SimulationLearningRepository repo;
    private final SimulationPlaybackClient playback;
    private final SimulationLearningClock clock;
    @Value("${simulation.learning-enabled:true}") private boolean enabled=true;

    @Transactional
    public Attempt create(Create request) {
        var actor=learn();id(request.eventId());id(request.scenarioId());String license=SimulationLicensePolicy.require(request.licenseClass());SimulationBank.version(request.bankVersion());
        String hash=digest("CREATE",request.scenarioId(),license,request.bankVersion());repo.eventLock(actor.userId(),request.eventId());
        var old=repo.receipt(actor.userId(),request.eventId());
        if(old.isPresent()) {same(old.get(),null,"CREATE",hash,false);return view(repo.get(actor.userId(),old.get().attemptId(),false),clock.now());}
        var bank=banks.load(request.bankVersion());var scenario=SimulationBank.scenario(bank,request.scenarioId());var now=clock.now();
        var row=new Row();row.id=UUID.randomUUID();row.user=actor.userId();row.createEvent=request.eventId();row.bank=bank.bankVersion();row.scoring=bank.scoringVersion();row.manifest=bank.manifestSha256();
        row.snapshot=scenario;row.scenarioId=scenario.scenarioId();row.number=scenario.scenarioNumber();row.chapter=scenario.chapterId();row.license=license;row.duration=scenario.durationMs();
        int index=scenario.scenarioNumber();
        row.nextScenario=index<bank.scenarios().size() && bank.scenarios().get(index).chapterId()==row.chapter?bank.scenarios().get(index).scenarioId():null;
        row.status="CREATED";row.playbackId=UUID.randomUUID();row.created=now;row.updated=now;
        repo.insert(row);repo.receipt(row.user,request.eventId(),row.id,"CREATE",hash,now);return view(row,now);
    }
    @Transactional(readOnly=true)
    public Attempt get(UUID attempt) {var actor=access.read(false);return view(repo.get(actor.userId(),id(attempt),false),clock.now());}
    @Transactional(readOnly=true)
    public Result result(UUID attempt) {var actor=access.read(false);return SimulationLearningRules.result(repo.get(actor.userId(),id(attempt),false));}

    @Transactional
    public Playback playback(UUID attempt) {
        var actor=learn();access.read(true);var row=repo.get(actor.userId(),id(attempt),true);var now=clock.now();
        if(Set.of("COMPLETED","CANCELLED").contains(row.status)) throw conflict("SIMULATION_TERMINAL","Create a new attempt to replay");
        if("PLAYING".equals(row.status)&&!live(row,now)) throw conflict("SIMULATION_PLAYBACK_INTERRUPTED","Interrupt and resume the attempt before renewing playback");
        // Require the pinned bank to remain published before issuing a new URL.
        var bank=banks.load(row.bank);
        if(!row.manifest.equals(bank.manifestSha256())) throw conflict("SIMULATION_BANK_CHANGED","Pinned bank binding differs");
        var response=playback.resolve(actor,row);row.granted=clock.now();row.updated=row.granted;repo.save(row);
        return new Playback(row.id,row.playbackId,row.bank,response.url(),response.expiredAt(),"video/mp4",row.duration,row.lastMs);
    }
    @Transactional
    public Attempt start(UUID attempt,Event request) {
        return event(attempt,request,"START",r->{
            if(!Set.of("CREATED","INTERRUPTED").contains(r.status)) throw conflict("SIMULATION_ALREADY_STARTED","Attempt already started; read its current state");
            var now=clock.now();
            if(r.granted==null || Duration.between(r.granted,now).toMillis()<0 || Duration.between(r.granted,now).toMillis()>60000)
                throw conflict("SIMULATION_PLAYBACK_REQUIRED","Resolve playback immediately before start or resume");
            if(request.mediaTimeMs()!=r.lastMs) throw conflict("SIMULATION_RESUME_POSITION","Resume at the verified position");
            r.status="PLAYING";if(r.started==null)r.started=now;r.segmentStarted=now;r.segmentStart=r.lastMs;r.lastObserved=now;r.reason=null;
            repo.cursorStart(r,now);
        });
    }
    @Transactional
    public Attempt heartbeat(UUID attempt,Event request) {
        return event(attempt,request,"HEARTBEAT",r->{position(r,request.mediaTimeMs(),clock.now(),false);r.lastMs=request.mediaTimeMs();r.lastObserved=clock.now();});
    }
    @Transactional
    public Attempt response(UUID attempt,Event request) {
        return event(attempt,request,"RESPONSE",r->{
            if(r.responseMs!=null && Set.of("PLAYING","COMPLETED").contains(r.status)) return;
            position(r,request.mediaTimeMs(),clock.now(),true);
            r.responseMs=request.mediaTimeMs();r.responseEvent=request.eventId();r.lastMs=Math.max(r.lastMs,request.mediaTimeMs());r.lastObserved=clock.now();
        });
    }
    @Transactional
    public Attempt complete(UUID attempt,Event request) {
        return event(attempt,request,"COMPLETE",r->{
            if("COMPLETED".equals(r.status)) return;
            var now=clock.now();position(r,request.mediaTimeMs(),now,false);
            if(request.mediaTimeMs()<r.duration-END_TOLERANCE || r.segmentStart+Duration.between(r.segmentStarted,now).toMillis()<r.duration-END_TOLERANCE)
                throw conflict("SIMULATION_VIDEO_NOT_FINISHED","Video playback has not reached its end");
            r.lastMs=request.mediaTimeMs();r.lastObserved=now;r.status="COMPLETED";r.score=score(r.snapshot.scoringTimeline(),r.responseMs);r.completed=now;
            repo.cursorComplete(r,now);
        });
    }
    @Transactional
    public Attempt interrupt(UUID attempt,Interrupt request) {
        var actor=learn();id(request.eventId());id(request.playbackEventId());
        if(!Set.of("VIDEO_ERROR","NETWORK","PAGE_EXIT","PAUSED","SEEK").contains(request.reason()==null?"":request.reason())) throw SimulationFailure.invalid("Invalid interruption reason");
        String hash=digest("INTERRUPT",attempt,request.playbackEventId(),request.reason());
        if(replay(actor.userId(),request.eventId(),attempt,"INTERRUPT",hash)) return view(repo.get(actor.userId(),attempt,false),clock.now());
        var r=repo.get(actor.userId(),id(attempt),true);nonce(r,request.playbackEventId());
        if(!"PLAYING".equals(r.status)) throw conflict("SIMULATION_NOT_PLAYING","Only playing attempts can be interrupted");
        r.status="INTERRUPTED";r.reason=request.reason();r.playbackId=UUID.randomUUID();r.granted=null;r.updated=clock.now();repo.save(r);
        repo.receipt(actor.userId(),request.eventId(),attempt,"INTERRUPT",hash,r.updated);return view(r,r.updated);
    }
    @Transactional
    public Attempt cancel(UUID attempt,Cancel request) {
        var actor=learn();id(request.eventId());String hash=digest("CANCEL",attempt);
        if(replay(actor.userId(),request.eventId(),attempt,"CANCEL",hash)) return view(repo.get(actor.userId(),attempt,false),clock.now());
        var r=repo.get(actor.userId(),id(attempt),true);
        if("COMPLETED".equals(r.status)) throw conflict("SIMULATION_TERMINAL","Completed results cannot be cancelled");
        if(!"CANCELLED".equals(r.status)) {r.status="CANCELLED";r.updated=clock.now();repo.save(r);}
        repo.receipt(actor.userId(),request.eventId(),attempt,"CANCEL",hash,clock.now());return view(r,clock.now());
    }
    @Transactional
    public Map<String,Object> bookmark(UUID scenario,Bookmark request) {
        var actor=learn();SimulationLicensePolicy.require(request.licenseClass());var bank=banks.load(request.bankVersion());SimulationBank.scenario(bank,id(scenario));
        if(request.bookmarked()==null)throw SimulationFailure.invalid("bookmarked is required");
        repo.bookmark(actor.userId(),bank.bankVersion(),scenario,request.bookmarked(),clock.now());
        return Map.of("scenarioId",scenario,"bankVersion",bank.bankVersion(),"bookmarked",request.bookmarked());
    }
    @Transactional
    public Cursor cursor(int chapter,CursorCommand request) {
        var actor=learn();id(request.eventId());id(request.scenarioId());SimulationLicensePolicy.require(request.licenseClass());
        String hash=digest("CURSOR",chapter,request.bankVersion(),request.scenarioId(),request.expectedRevision());
        if(replay(actor.userId(),request.eventId(),null,"CURSOR",hash))return repo.cursor(actor.userId(),request.bankVersion(),chapter);
        var bank=banks.load(request.bankVersion());var s=SimulationBank.scenario(bank,request.scenarioId());
        if(s.chapterId()!=chapter || request.expectedRevision()<0)throw SimulationFailure.invalid("Cursor must belong to the chapter and have a nonnegative revision");
        if(!repo.cursorSet(actor.userId(),bank.bankVersion(),chapter,s.scenarioId(),request.expectedRevision(),clock.now())) throw conflict("SIMULATION_CURSOR_CONFLICT","Cursor changed; read the current revision and retry");
        repo.receipt(actor.userId(),request.eventId(),null,"CURSOR",hash,clock.now());return repo.cursor(actor.userId(),bank.bankVersion(),chapter);
    }
    private Attempt event(UUID attempt,Event request,String kind,java.util.function.Consumer<Row> action) {
        var actor=learn();id(attempt);id(request.eventId());id(request.playbackEventId());
        if(request.mediaTimeMs()==null || request.mediaTimeMs()<0)throw SimulationFailure.invalid("mediaTimeMs is required and nonnegative");
        String hash=digest(kind,attempt,request.playbackEventId(),request.mediaTimeMs());
        if(replay(actor.userId(),request.eventId(),attempt,kind,hash))return view(repo.get(actor.userId(),attempt,false),clock.now());
        var row=repo.get(actor.userId(),attempt,true);nonce(row,request.playbackEventId());
        if(request.mediaTimeMs()>row.duration)throw SimulationFailure.invalid("mediaTimeMs outside the video");
        boolean terminal="COMPLETED".equals(row.status);Integer oldResponse=row.responseMs;
        action.accept(row);
        // New repeats of complete/Space acknowledge the existing terminal result without rewriting it.
        if(!terminal && !("RESPONSE".equals(kind)&&oldResponse!=null)) {row.updated=clock.now();repo.save(row);}
        repo.receipt(actor.userId(),request.eventId(),attempt,kind,hash,clock.now());return view(row,clock.now());
    }
    private boolean replay(UUID user,UUID event,UUID attempt,String kind,String hash) {
        repo.eventLock(user,event);var old=repo.receipt(user,event);
        if(old.isEmpty())return false;same(old.get(),attempt,kind,hash,true);return true;
    }
    private static void same(SimulationLearningRepository.Receipt r,UUID attempt,String kind,String hash,boolean compareAttempt) {
        if(!kind.equals(r.kind()) || !hash.equals(r.digest()) || (compareAttempt&&!Objects.equals(attempt,r.attemptId()))) throw conflict("SIMULATION_EVENT_CONFLICT","eventId already belongs to a different request");
    }
    private SimulationAccess.Actor learn() {if(!enabled)throw SimulationFailure.unavailable();return access.learn();}
    private static UUID id(UUID value) {if(value==null)throw SimulationFailure.invalid("UUID is required");return value;}
    private static void nonce(Row r,UUID playback) {if(!r.playbackId.equals(playback))throw conflict("SIMULATION_PLAYBACK_CONFLICT","Playback session changed; read the current attempt");}
    private String digest(Object...values) {
        try {return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(repo.encode(Arrays.asList(values)).getBytes(StandardCharsets.UTF_8)));}
        catch(Exception e) {throw new IllegalStateException("Cannot fingerprint simulation request");}
    }
}
