package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import static com.dts.practice.simulation.SimulationShortcutModel.*;

@Service
@RequiredArgsConstructor
public class SimulationShortcutService {
    private final SimulationAccess access;private final SimulationExamAccess examAccess;
    private final SimulationLearningRepository learning;private final SimulationExamRepository exams;
    private final SimulationShortcutRepository shortcuts;private final SimulationLearningClock clock;
    @Value("${simulation.video-shortcuts-enabled:true}")private boolean enabled=true;
    @Value("${simulation.learning-enabled:true}")private boolean learningEnabled=true;
    @Value("${simulation.exams-enabled:true}")private boolean examsEnabled=true;
    private SimulationAccess.Actor actor(String kind){if(kind==null||!Set.of("LEARNING","EXAM").contains(kind))throw SimulationShortcutFailure.invalid();if("EXAM".equals(kind)){if(!examsEnabled)throw SimulationShortcutFailure.unavailable();return examAccess.require(false);}if(!learningEnabled)throw SimulationShortcutFailure.unavailable();return access.learn();}
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public State state(String kind,UUID target,String license){var a=actor(kind);String code=SimulationLicensePolicy.require(license);return load(kind,id(target),a.userId(),code,false,shortcuts.ready()).state();}
    @Transactional
    public Ack enable(String kind,UUID target,Request request,String license){return command(kind,target,request,license,"ENABLE");}
    @Transactional
    public Ack skip(String kind,UUID target,Request request,String license){return command(kind,target,request,license,"SKIP_INTRO");}
    @Transactional
    public Ack advance(UUID target,Request request,String license){return command("EXAM",target,request,license,"ADVANCE_AFTER_REACTION");}
    private Ack command(String kind,UUID target,Request req,String license,String operation) {
        var a=actor(kind);String code=SimulationLicensePolicy.require(license);id(target);if(req==null)throw SimulationShortcutFailure.invalid();id(req.eventId());id(req.playbackEventId());
        if(req.position()<1||req.position()>("EXAM".equals(kind)?10:1))throw SimulationShortcutFailure.invalid();
        if("SKIP_INTRO".equals(operation)&&(req.mediaTimeMs()==null||req.mediaTimeMs()<0))throw SimulationShortcutFailure.invalid();
        if(!enabled||!shortcuts.ready())throw SimulationShortcutFailure.unavailable();
        String hash=digest(Arrays.asList(operation,kind,target,code,req.position(),req.playbackEventId(),req.mediaTimeMs(),req.reactionAcknowledgement()));
        shortcuts.lock(a.userId(),req.eventId());
        if("EXAM".equals(kind))exams.lock(a.userId());else learning.eventLock(a.userId(),req.eventId());
        var old=shortcuts.event(a.userId(),req.eventId());
        if(old!=null){if(!old.kind().equals(kind)||!old.target().equals(target)||!old.operation().equals(operation)||!old.hash().equals(hash))throw SimulationShortcutFailure.conflict("EVENT_CONFLICT");return new Ack(old.receipt(),load(kind,target,a.userId(),code,true,true).state());}
        if("EXAM".equals(kind)?exams.receipt(a.userId(),req.eventId()).isPresent():learning.receipt(a.userId(),req.eventId()).isPresent())throw SimulationShortcutFailure.conflict("EVENT_CONFLICT");
        var t=load(kind,target,a.userId(),code,true,true);t.match(req);
        Protocol p=t.protocol;
        if("ENABLE".equals(operation)) {
            if(p==null){if(!t.canEnable())throw SimulationShortcutFailure.conflict("POLICY_LOCKED");p=new Protocol();p.kind=kind;p.target=target;p.user=a.userId();p.secret=UUID.randomUUID();p.revision=1;shortcuts.insert(p,clock.now());t.protocol=p;}
            // Another enable intent is a safe no-op; the immutable protocol is not reset.
        } else {
            if(p==null)throw SimulationShortcutFailure.conflict("NOT_ENABLED");
            if("SKIP_INTRO".equals(operation)) {
                var cap=t.intro();if(!cap.available())throw SimulationShortcutFailure.conflict(cap.reason());
                if(req.mediaTimeMs()>=SimulationShortcutRules.INTRO_MS)throw SimulationShortcutFailure.conflict("INTRO_ALREADY_PASSED");
                t.validatePosition(req.mediaTimeMs());t.skip();p.skipped.add(req.position());p.revision++;shortcuts.save(p);
            } else {
                if(t.frame==null||t.frame.responseMs==null)throw SimulationShortcutFailure.conflict("REACTION_ACK_REQUIRED");
                if(req.reactionAcknowledgement()==null||!MessageDigest.isEqual(req.reactionAcknowledgement().getBytes(StandardCharsets.UTF_8),t.reactionAcknowledgement().getBytes(StandardCharsets.UTF_8)))throw SimulationShortcutFailure.conflict("REACTION_ACK_REQUIRED");
                t.advance();p.reasons.put(Integer.toString(req.position()),"ADVANCE_AFTER_REACTION");p.revision++;shortcuts.save(p);
            }
        }
        State current=t.state();var receipt=new Receipt(req.eventId(),operation,kind,target,req.position(),req.playbackEventId(),current.playbackEventId(),"SKIP_INTRO".equals(operation)?10000:0,p.revision,clock.now());
        shortcuts.event(a.userId(),hash,receipt);
        // Reserve the legacy event namespace too: an old command cannot reuse this UUID.
        String marker="ENABLE".equals(operation)?"SHORTCUT_ENABLE":"SKIP_INTRO".equals(operation)?"SHORTCUT_SKIP":"SHORTCUT_ADVANCE";
        if(t.exam!=null)exams.receipt(a.userId(),req.eventId(),target,marker,hash,current.session(),clock.now());
        else learning.receipt(a.userId(),req.eventId(),target,marker,hash,clock.now());
        return new Ack(receipt,current);
    }
    private Target load(String kind,UUID target,UUID user,String license,boolean lock,boolean ready) {
        var t=new Target();t.kind=kind;t.id=target;t.user=user;t.license=license;t.schema=ready;
        if("EXAM".equals(kind)){t.exam=exams.get(user,target,lock);t.frames=exams.frames(target);t.frame=t.frames.get(t.exam.currentPosition-1);}
        else t.attempt=learning.get(user,target,lock);
        t.protocol=ready?shortcuts.get(kind,target):null;
        if(t.protocol!=null&&!t.protocol.user.equals(user))throw SimulationShortcutFailure.conflict("OWNER_MISMATCH");
        return t;
    }
    private final class Target {
        String kind,license;UUID id,user;boolean schema;Protocol protocol;
        SimulationLearningModel.Row attempt;SimulationExamModel.Row exam;List<SimulationExamModel.Frame> frames;SimulationExamModel.Frame frame;
        int position(){return exam==null?1:exam.currentPosition;}
        UUID nonce(){return exam==null?attempt.playbackId:frame.playbackId;}
        Integer response(){return exam==null?attempt.responseMs:frame.responseMs;}
        boolean active(){return exam==null?!Set.of("COMPLETED","CANCELLED").contains(attempt.status):"IN_PROGRESS".equals(exam.status);}
        boolean playing(){return exam==null?"PLAYING".equals(attempt.status)&&SimulationLearningRules.live(attempt,clock.now()):"PLAYING".equals(frame.state)&&SimulationExamRules.live(frame,clock.now());}
        int last(){return exam==null?attempt.lastMs:frame.lastMs;}
        int checkpoint(){return exam==null?attempt.lastMs:frame.segmentStart;}
        boolean canEnable(){return active()&&(exam==null?"CREATED".equals(attempt.status)&&attempt.started==null&&last()==0&&response()==null:exam.completedCount==0&&exam.currentPosition==1&&frames.stream().allMatch(f->Set.of("NOT_STARTED","READY").contains(f.state)&&f.segmentStarted==null&&f.responseMs==null&&!f.complete&&f.lastMs==0));}
        void match(Request req){if(!active())throw SimulationShortcutFailure.conflict("TERMINAL");if(position()!=req.position())throw SimulationShortcutFailure.conflict("POSITION_CHANGED");if(!nonce().equals(req.playbackEventId()))throw SimulationShortcutFailure.conflict("PLAYBACK_CHANGED");}
        SimulationShortcutRules.Intro intro(){var snapshot=exam==null?attempt.snapshot:frame.snapshot;var c=SimulationShortcutRules.intro(snapshot.durationMs(),snapshot.scoringTimeline().t5StartMs(),enabled&&schema&&protocol!=null&&active(),playing(),SimulationShortcutRules.usedInPlayback(exam!=null,protocol!=null&&protocol.skipped.contains(position()),response(),checkpoint()),response(),last());String reason=!schema?"MIGRATION_REQUIRED":!enabled?"FEATURE_DISABLED":!active()?"TERMINAL":c.reason();return new SimulationShortcutRules.Intro(reason==null,c.targetMs(),c.maxSafeTargetMs(),reason);}
        void validatePosition(int ms){if(exam==null)SimulationLearningRules.position(attempt,ms,clock.now(),false);else SimulationExamRules.position(frame,ms,clock.now(),false);}
        String reactionAcknowledgement(){
            if(protocol==null||response()==null||!active()||exam==null)return null;
            try {Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(protocol.secret.toString().getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return HexFormat.of().formatHex(mac.doFinal((kind+":"+id+":"+nonce()+":"+frame.responseEvent+":"+response()).getBytes(StandardCharsets.UTF_8)));}
            catch(Exception e){throw new IllegalStateException("Cannot confirm reaction");}
        }
        void skip(){
            if(exam==null){attempt.status="INTERRUPTED";attempt.reason="SHORTCUT_CHECKPOINT";attempt.playbackId=UUID.randomUUID();attempt.granted=null;attempt.segmentStarted=null;attempt.lastObserved=null;attempt.segmentStart=10000;attempt.lastMs=10000;attempt.updated=clock.now();learning.save(attempt);}
            else {frame.playbackId=UUID.randomUUID();frame.state="NOT_STARTED";frame.granted=null;frame.segmentStarted=null;frame.lastObserved=null;frame.lastMs=10000;frame.segmentStart=10000;frame.checkpointColumn=true;exams.save(frame);exam.updated=clock.now();exams.save(exam);}
        }
        void advance(){
            frame.complete=true;frame.completed=clock.now();frame.score=SimulationExamRules.score(frame);frame.state="FINISHED";exams.save(frame);
            exam.completedCount++;exam.currentPosition=Math.min(10,exam.completedCount+1);if(exam.completedCount==10)exam.status="READY_TO_SUBMIT";exam.updated=clock.now();exams.save(exam);frame=frames.get(exam.currentPosition-1);
        }
        Map<String,String> completionReasons(){var result=new TreeMap<String,String>();if(exam==null){if("COMPLETED".equals(attempt.status))result.put("1","ENDED");}else for(var f:frames)if(f.complete)result.put(Integer.toString(f.position),protocol==null?"ENDED":protocol.reasons.getOrDefault(Integer.toString(f.position),"ENDED"));return Map.copyOf(result);}
        State state(){
            var session=exam==null?null:SimulationExamRules.view(exam,frames,license,clock.now());var value=attempt==null?null:SimulationLearningRules.view(attempt,clock.now());
            boolean support=enabled&&schema,on=protocol!=null;String reason=!schema?"MIGRATION_REQUIRED":!enabled?"FEATURE_DISABLED":!on?(canEnable()?"NOT_ENABLED":"LEGACY_ALREADY_STARTED"):null;
            String nextReason=!support?reason:!on?"NOT_ENABLED":exam==null?"EXAMS_ONLY":!active()?"TERMINAL":response()==null?"REACTION_ACK_REQUIRED":null;
            String completion=protocol==null?null:protocol.reasons.get(Integer.toString(position()));if(completion==null&&(exam==null?"COMPLETED".equals(attempt.status):frame.complete))completion="ENDED";
            return new State(kind,id,license,support,on,support&&!on&&canEnable(),on?SimulationShortcutRules.VERSION:"legacy-v1",reason,on?protocol.revision:0,
                    digest(Arrays.asList(session,value,on?protocol.revision:0,last(),checkpoint())),position(),nonce(),checkpoint(),intro(),new Advance(nextReason==null,nextReason),nextReason==null?reactionAcknowledgement():null,completion,completionReasons(),value,session);
        }
    }
    private static UUID id(UUID value){if(value==null)throw SimulationShortcutFailure.invalid();return value;}
    private String digest(Object value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(shortcuts.encode(value).getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException("Cannot fingerprint shortcut request");}}
}
