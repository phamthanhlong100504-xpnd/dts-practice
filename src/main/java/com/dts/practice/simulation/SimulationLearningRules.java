package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import org.springframework.http.HttpStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import static com.dts.practice.simulation.SimulationLearningModel.*;

final class SimulationLearningRules {
    static final int GAP=12000,TOLERANCE=1500,RESPONSE_LAG=3000,END_TOLERANCE=250;
    static final Policy POLICY=new Policy(false,false,true,false,3000,GAP);
    private SimulationLearningRules() {}
    static BusinessException conflict(String code,String message) {return BusinessException.of(code,message,HttpStatus.CONFLICT);}
    static boolean live(Row row,Instant now) {return row.lastObserved!=null && !now.isBefore(row.lastObserved) && Duration.between(row.lastObserved,now).toMillis()<=GAP;}
    static void position(Row row,int position,Instant now,boolean response) {
        if(position<0 || position>row.duration) throw SimulationFailure.invalid("mediaTimeMs outside the video");
        if(!"PLAYING".equals(row.status)) throw conflict("SIMULATION_NOT_PLAYING","Attempt is not playing");
        if(!live(row,now)) throw conflict("SIMULATION_PLAYBACK_INTERRUPTED","Playback continuity lost; interrupt and resume from the verified position");
        long expected=SimulationShortcutRules.expectedAbsoluteMs(row.segmentStart,Duration.between(row.segmentStarted,now).toMillis());
        if(position<row.segmentStart || position>expected+TOLERANCE || position<expected-(response?RESPONSE_LAG:TOLERANCE)
                || (!response && position<row.lastMs) || (response && position<row.lastMs-RESPONSE_LAG))
            throw conflict("SIMULATION_PLAYBACK_DISCONTINUITY","Video position does not match continuous playback");
    }
    static int score(SimulationBank.Timeline t,Integer response) {
        if(response==null || response<t.t5StartMs() || response>t.t0EndMs()) return 0;
        if(response<t.t4StartMs()) return 5;if(response<t.t3StartMs()) return 4;if(response<t.t2StartMs()) return 3;
        if(response<t.t1StartMs()) return 2;return 1;
    }
    static List<Segment> segments(SimulationBank.Timeline t) {
        return List.of(new Segment(t.t5StartMs(),t.t4StartMs(),5,false),new Segment(t.t4StartMs(),t.t3StartMs(),4,false),
                new Segment(t.t3StartMs(),t.t2StartMs(),3,false),new Segment(t.t2StartMs(),t.t1StartMs(),2,false),new Segment(t.t1StartMs(),t.t0EndMs(),1,true));
    }
    static Result result(Row r) {
        if(!"COMPLETED".equals(r.status)) throw conflict("SIMULATION_RESULT_NOT_READY","Result is available after valid completion");
        return new Result(r.id,r.scenarioId,r.status,r.score,5,r.responseMs!=null,r.responseMs,r.duration,r.bank,r.scoring,
                segments(r.snapshot.scoringTimeline()),r.snapshot.hazardSign(),r.snapshot.recognitionTip());
    }
    static Attempt view(Row r,Instant now) {
        return new Attempt(r.id,r.scenarioId,r.bank,r.scoring,r.status,r.playbackId,r.duration,r.lastMs,r.responseMs!=null,r.responseMs,
                "INTERRUPTED".equals(r.status) || ("PLAYING".equals(r.status)&&!live(r,now)),r.created,r.started,r.completed,POLICY,
                "COMPLETED".equals(r.status)?result(r):null);
    }
}
