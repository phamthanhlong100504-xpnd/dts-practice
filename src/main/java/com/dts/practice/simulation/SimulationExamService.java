package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.function.BiConsumer;
import static com.dts.practice.simulation.SimulationExamModel.*;
import static com.dts.practice.simulation.SimulationExamRules.*;

@Service
@RequiredArgsConstructor
public class SimulationExamService {
    private final SimulationExamAccess access;private final SimulationBankClient banks;private final SimulationExamRepository repo;
    private final SimulationExamMediaClient media;private final SimulationExamRules rules;private final SimulationLearningClock clock;
    @Value("${simulation.exams-enabled:true}")private boolean enabled=true;
    private SimulationAccess.Actor actor(boolean media){if(!enabled)throw SimulationExamFailure.unavailable();return access.require(media);}
    @Transactional(readOnly=true)
    public Preparation preparation(String license) {
        var actor=actor(false);String code=SimulationLicensePolicy.require(license);var bank=banks.current();
        return new Preparation(new Scope(actor.userId(),code,bank.bankVersion()),bank.bankVersion(),rules.currentPolicy(),repo.active(actor.userId()).map(r->view(r,code)).orElse(null));
    }
    @Transactional(readOnly=true)
    public Active active(String license){var a=actor(false);if(license!=null)SimulationLicensePolicy.require(license);return new Active(repo.active(a.userId()).map(r->view(r,license)).orElse(null));}
    @Transactional(readOnly=true)
    public Session get(UUID session,String license){var a=actor(false);return view(repo.get(a.userId(),id(session),false),license);}
    @Transactional
    public Session create(UUID key,Create request) {
        var a=actor(true);id(key);if(request==null||request.mode()==null||!Set.of("RANDOM","PRESET").contains(request.mode()))throw SimulationExamFailure.invalid("Mode must be RANDOM or PRESET");
        boolean preset="PRESET".equals(request.mode());
        if(preset){id(request.presetSetId());id(request.presetVersionId());}
        else if(request.presetSetId()!=null||request.presetVersionId()!=null)throw SimulationExamFailure.invalid("RANDOM cannot select a preset");
        String license=SimulationLicensePolicy.require(request.licenseClass());if(request.bankVersion()!=null)SimulationBank.version(request.bankVersion());
        String hash=preset?digest("CREATE",request.mode(),license,request.bankVersion(),request.presetSetId(),request.presetVersionId()):digest("CREATE",request.mode(),license,request.bankVersion());repo.lock(a.userId());var previous=repo.created(a.userId(),key);
        if(previous.isPresent()){if(!previous.get().createHash.equals(hash))throw eventConflict();return view(previous.get(),license);}
        var active=repo.active(a.userId());if(active.isPresent())throw SimulationExamFailure.active(active.get().id);
        var bank=banks.current();SimulationBank.validate(new SimulationBank.Published(bank,"PUBLISHED",true,clock.now()),null);
        if(request.bankVersion()!=null&&!request.bankVersion().equals(bank.bankVersion()))throw SimulationExamFailure.conflict("SIMULATION_EXAM_BANK_CHANGED","Read current preparation before creating an exam");
        SimulationPresets.Version source=null;
        if(preset){source=SimulationPresets.validate(banks.preset(request.presetVersionId()),bank);
            if(!source.presetSetId().equals(request.presetSetId())||!source.presetVersionId().equals(request.presetVersionId()))throw SimulationExamFailure.conflict("SIMULATION_PRESET_VERSION_CHANGED","Read the current preset before starting");}
        var ordered=preset?source.snapshot().scenarios():rules.shuffled(bank);List<Frame> frames=new ArrayList<>();Set<UUID> selected=new HashSet<>();
        for(int offset=0;offset<ordered.size()&&frames.size()<10;) {
            int count=Math.min(10-frames.size(),ordered.size()-offset);var batch=List.copyOf(ordered.subList(offset,offset+count));offset+=count;
            var eligible=media.eligibility(a,bank,batch);
            for(int i=0;i<batch.size();i++)if(eligible.get(i).ready()) {
                var s=batch.get(i);if(!selected.add(s.scenarioId()))throw SimulationExamFailure.unavailable();var f=new Frame();
                f.position=frames.size()+1;f.scenarioId=s.scenarioId();f.snapshot=s;f.chapterName=bank.chapters().get(s.chapterId()-1).name();f.duration=s.durationMs();f.etag=eligible.get(i).objectEtag();
                f.playbackId=UUID.randomUUID();f.state="NOT_STARTED";frames.add(f);
            }
        }
        if(frames.size()!=10)throw SimulationExamFailure.conflict(preset?"SIMULATION_PRESET_MEDIA_UNAVAILABLE":"SIMULATION_EXAM_NOT_ENOUGH_MEDIA",preset?"All ten fixed preset videos must be READY; no substitution is allowed":"At least ten verified READY videos are required");
        if(preset&&!source.equals(SimulationPresets.validate(banks.preset(request.presetVersionId()),bank)))throw SimulationExamFailure.conflict("SIMULATION_PRESET_VERSION_CHANGED","Preset publication changed before creation");
        var p=preset?SimulationExamRules.PRESET_POLICY:rules.currentPolicy();if(p.totalScenarios()!=10||p.maxScenarioScore()!=5||p.maxTotalScore()!=50||p.passingScore()<0||p.passingScore()>50||p.policyVersion()==null||p.policyVersion().isBlank())throw SimulationExamFailure.unavailable();
        var row=new Row();row.id=UUID.randomUUID();row.user=a.userId();row.createKey=key;row.createHash=hash;row.license=license;row.bank=bank.bankVersion();row.scoring=bank.scoringVersion();row.manifest=bank.manifestSha256();
        row.mode=request.mode();row.preset=preset?SimulationPresets.ref(source):null;
        row.policyVersion=p.policyVersion();row.passingScore=p.passingScore();row.status="IN_PROGRESS";row.completedCount=0;row.currentPosition=1;row.created=clock.now();row.started=row.created;row.updated=row.created;
        for(var f:frames)f.session=row.id;repo.insert(row,frames);return SimulationExamRules.view(row,frames,license,clock.now());
    }
    @Transactional
    public Playback playback(UUID session,String license) {
        if(license!=null)SimulationLicensePolicy.require(license);
        var a=actor(true);repo.lock(a.userId());var r=repo.get(a.userId(),id(session),true);checkCurrent(r,r.currentPosition);var f=repo.frame(r.id,r.currentPosition);
        if("INTERRUPTED".equals(f.state)||("PLAYING".equals(f.state)&&!live(f,clock.now())))throw SimulationExamFailure.conflict("SIMULATION_EXAM_RESTART_REQUIRED","Restart the current video from zero while keeping its first reaction");
        var video=media.playback(a,r,f,false);f.granted=clock.now();if("NOT_STARTED".equals(f.state))f.state="READY";repo.save(f);
        return new Playback(r.id,f.position,f.playbackId,video.url(),video.expiredAt(),"video/mp4",f.duration,f.responseMs!=null,f.segmentStart);
    }
    @Transactional
    public Session restart(UUID session,Restart request,String license) {
        var a=actor(false);if(request==null)throw SimulationExamFailure.invalid("Restart event is required");id(request.eventId());id(request.playbackEventId());
        String hash=digest("RESTART",session,request.position(),request.playbackEventId(),license);repo.lock(a.userId());var old=receipt(a.userId(),request.eventId(),session,"RESTART",hash);if(old!=null)return old;
        var r=repo.get(a.userId(),id(session),true);checkCurrent(r,request.position());var f=repo.frame(r.id,request.position());nonce(f,request.playbackEventId());
        f.playbackId=UUID.randomUUID();f.state="NOT_STARTED";f.granted=null;f.segmentStarted=null;f.lastObserved=null;f.lastMs=0;f.segmentStart=0;repo.save(f);
        return ack(r,a.userId(),request.eventId(),"RESTART",hash,license);
    }
    @Transactional
    public Session start(UUID session,Event event,String license) {
        return event(session,event,"START",license,(r,f)->{
            if(!Set.of("READY","NOT_STARTED").contains(f.state)||event.mediaTimeMs()!=f.segmentStart)throw SimulationExamFailure.conflict("SIMULATION_EXAM_START_CHANGED","Resolve playback then start at the server-approved checkpoint");
            if(f.granted==null||clock.now().isBefore(f.granted)||Duration.between(f.granted,clock.now()).toMillis()>60000)throw SimulationExamFailure.conflict("SIMULATION_EXAM_PLAYBACK_REQUIRED","Resolve current playback immediately before starting");
            f.state="PLAYING";f.segmentStarted=clock.now();f.lastObserved=clock.now();f.lastMs=f.segmentStart;
        });
    }
    @Transactional
    public Session heartbeat(UUID session,Event event,String license){return event(session,event,"HEARTBEAT",license,(r,f)->{position(f,event.mediaTimeMs(),clock.now(),false);f.lastMs=event.mediaTimeMs();f.lastObserved=clock.now();});}
    @Transactional
    public Session reaction(UUID session,Event event,String license){return event(session,event,"REACTION",license,(r,f)->{
        if(f.responseMs!=null)return;position(f,event.mediaTimeMs(),clock.now(),true);f.responseMs=event.mediaTimeMs();f.responseEvent=event.eventId();f.lastMs=Math.max(f.lastMs,event.mediaTimeMs());f.lastObserved=clock.now();
    });}
    @Transactional
    public Session complete(UUID session,Event event,String license){return event(session,event,"COMPLETE_CURRENT",license,(r,f)->{
        position(f,event.mediaTimeMs(),clock.now(),false);
        if(event.mediaTimeMs()<f.duration-SimulationLearningRules.END_TOLERANCE||f.segmentStart+Duration.between(f.segmentStarted,clock.now()).toMillis()<f.duration-SimulationLearningRules.END_TOLERANCE)
            throw SimulationExamFailure.conflict("SIMULATION_EXAM_VIDEO_NOT_FINISHED","Continuous verified playback has not reached the end");
        f.lastMs=event.mediaTimeMs();f.lastObserved=clock.now();f.complete=true;f.completed=clock.now();f.score=score(f);f.state="FINISHED";
        r.completedCount++;r.currentPosition=Math.min(10,r.completedCount+1);if(r.completedCount==10)r.status="READY_TO_SUBMIT";
    });}
    private Session event(UUID session,Event event,String kind,String license,BiConsumer<Row,Frame> action) {
        var a=actor(false);if(event==null||event.mediaTimeMs()==null||event.mediaTimeMs()<0||event.position()<1||event.position()>10)throw SimulationExamFailure.invalid("A valid position, event and media time are required");
        id(session);id(event.eventId());id(event.playbackEventId());String hash=digest(kind,session,event.position(),event.playbackEventId(),event.mediaTimeMs(),license);repo.lock(a.userId());
        var old=receipt(a.userId(),event.eventId(),session,kind,hash);if(old!=null)return old;
        var r=repo.get(a.userId(),session,true);var f=repo.frame(r.id,event.position());nonce(f,event.playbackEventId());
        if(event.mediaTimeMs()>f.duration)throw SimulationExamFailure.invalid("mediaTimeMs outside the video");
        if("COMPLETE_CURRENT".equals(kind)&&f.complete)return ack(r,a.userId(),event.eventId(),kind,hash,license);
        checkCurrent(r,event.position());Integer priorResponse=f.responseMs;action.accept(r,f);
        if(!("REACTION".equals(kind)&&priorResponse!=null)){repo.save(f);r.updated=clock.now();repo.save(r);}
        return ack(r,a.userId(),event.eventId(),kind,hash,license);
    }
    @Transactional
    public Session submit(UUID session,Command request,String license) {
        var a=actor(false);if(request==null)throw SimulationExamFailure.invalid("eventId is required");id(request.eventId());String hash=digest("SUBMIT",session,license);repo.lock(a.userId());
        var old=receipt(a.userId(),request.eventId(),session,"SUBMIT",hash);if(old!=null)return old;var r=repo.get(a.userId(),id(session),true);
        if("COMPLETED".equals(r.status))return ack(r,a.userId(),request.eventId(),"SUBMIT",hash,license);
        if(!"READY_TO_SUBMIT".equals(r.status)||r.completedCount!=10)throw SimulationExamFailure.conflict("SIMULATION_EXAM_INCOMPLETE","Ten valid completed videos are required");
        var frames=repo.frames(r.id);if(frames.size()!=10||frames.stream().anyMatch(f->!f.complete||f.score==null))throw SimulationExamFailure.unavailable();
        int total=frames.stream().mapToInt(f->f.score).sum();boolean passed=total>=r.passingScore;r.status="COMPLETED";r.completed=clock.now();r.updated=r.completed;
        var result=new Result(new Scope(r.user,r.license,r.bank),r.id,r.mode,"COMPLETED",r.bank,r.scoring,policy(r),10,10,total,50,r.passingScore,passed,passed?"PASSED":"FAILED",r.started,r.completed,
                frames.stream().map(f->new ResultItem(f.position,f.score,5)).toList(),r.preset);
        repo.result(r,result);repo.save(r);return ack(r,a.userId(),request.eventId(),"SUBMIT",hash,license);
    }
    @Transactional
    public Session cancel(UUID session,Command request,String license) {
        var a=actor(false);if(request==null)throw SimulationExamFailure.invalid("eventId is required");id(request.eventId());String hash=digest("CANCEL",session,license);repo.lock(a.userId());
        var old=receipt(a.userId(),request.eventId(),session,"CANCEL",hash);if(old!=null)return old;var r=repo.get(a.userId(),id(session),true);
        if("COMPLETED".equals(r.status))throw SimulationExamFailure.conflict("SIMULATION_EXAM_TERMINAL","A completed result cannot be cancelled");
        if(!"CANCELLED".equals(r.status)){r.status="CANCELLED";r.cancelled=clock.now();r.updated=r.cancelled;repo.save(r);}
        return ack(r,a.userId(),request.eventId(),"CANCEL",hash,license);
    }
    @Transactional(readOnly=true)
    public Result result(UUID session,String license){var a=actor(false);var r=completed(a.userId(),session);return result(r,license);}
    @Transactional(readOnly=true)
    public Review review(UUID session,String license) {
        var a=actor(false);var r=completed(a.userId(),session);var result=result(r,license);
        return new Review(result,repo.frames(r.id).stream().map(f->new ReviewItem(f.position,f.scenarioId,f.snapshot.scenarioNumber(),f.snapshot.title(),f.chapterName,f.score,5,
                f.responseMs!=null,f.responseMs,f.duration,SimulationLearningRules.segments(f.snapshot.scoringTimeline()),f.snapshot.hazardSign(),f.snapshot.recognitionTip(),r.scoring,
                "/api/v1/simulation-exams/sessions/"+r.id+"/review/"+f.position+"/playback")).toList());
    }
    @Transactional(readOnly=true)
    public Playback reviewPlayback(UUID session,int position,String license) {
        if(license!=null)SimulationLicensePolicy.require(license);
        var a=actor(true);var r=completed(a.userId(),session);if(position<1||position>10)throw SimulationExamFailure.invalid("Review position1..10 is required");
        var f=repo.frame(r.id,position);var v=media.playback(a,r,f,true);return new Playback(r.id,f.position,f.playbackId,v.url(),v.expiredAt(),"video/mp4",f.duration,f.responseMs!=null,0);
    }
    private Row completed(UUID user,UUID id){var r=repo.get(user,id(id),false);if(!"COMPLETED".equals(r.status))throw SimulationExamFailure.conflict("NOT_COMPLETED","Result and review require a completed exam");return r;}
    private Result result(Row row,String license) {
        Result r=repo.result(row.id);String code=license==null?row.license:SimulationLicensePolicy.require(license);
        return new Result(new Scope(row.user,code,row.bank),r.sessionId(),r.mode(),r.status(),r.bankVersion(),r.scoringVersion(),r.policy(),r.totalScenarios(),r.completedScenarios(),r.totalScore(),r.maxTotalScore(),r.passingScore(),r.passed(),r.resultStatus(),r.startedAt(),r.completedAt(),r.items(),r.preset());
    }
    private Session view(Row row,String license){return SimulationExamRules.view(row,repo.frames(row.id),license,clock.now());}
    private Session ack(Row row,UUID user,UUID event,String kind,String hash,String license){Session value=view(row,license);repo.receipt(user,event,row.id,kind,hash,value,clock.now());return value;}
    private Session receipt(UUID user,UUID event,UUID session,String kind,String hash) {
        var old=repo.receipt(user,event);if(old.isEmpty())return null;var r=old.get();if(!r.session().equals(session)||!r.kind().equals(kind)||!r.hash().equals(hash))throw eventConflict();return repo.decode(r.response(),Session.class);
    }
    private SimulationExamFailure eventConflict(){return SimulationExamFailure.conflict("SIMULATION_EXAM_EVENT_CONFLICT","Idempotency key already belongs to a different request");}
    private String digest(Object...data){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(repo.encode(Arrays.asList(data)).getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException("Cannot fingerprint exam request");}}
}
