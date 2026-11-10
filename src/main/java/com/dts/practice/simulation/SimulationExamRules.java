package com.dts.practice.simulation;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import static com.dts.practice.simulation.SimulationExamModel.*;

@Component
public class SimulationExamRules {
    static final Policy CURRENT_POLICY=new Policy(10,5,50,35,"simulation-random-10-v1-pass35");
    static final Policy PRESET_POLICY=new Policy(10,5,50,35,"simulation-preset-10-v1-pass35");
    Policy currentPolicy(){return CURRENT_POLICY;}
    List<SimulationBank.Scenario> shuffled(SimulationBank.Snapshot bank) {var rows=new ArrayList<>(bank.scenarios());Collections.shuffle(rows,new SecureRandom());return rows;}
    static Policy policy(Row row){return new Policy(10,5,50,row.passingScore,row.policyVersion);}
    static void checkCurrent(Row row,int position) {
        if(!"IN_PROGRESS".equals(row.status))throw SimulationExamFailure.conflict("SIMULATION_EXAM_TERMINAL","An active current video is required");
        if(row.currentPosition!=position)throw SimulationExamFailure.conflict("SIMULATION_EXAM_POSITION_CHANGED","Read the current session before retrying");
    }
    static void nonce(Frame frame,UUID nonce){if(nonce==null||!nonce.equals(frame.playbackId))throw SimulationExamFailure.conflict("SIMULATION_EXAM_PLAYBACK_CHANGED","Playback changed; read the session");}
    static SimulationLearningModel.Row learning(Frame f) {
        var row=new SimulationLearningModel.Row();row.duration=f.duration;row.status=f.state;row.segmentStart=f.segmentStart;row.segmentStarted=f.segmentStarted;row.lastObserved=f.lastObserved;row.lastMs=f.lastMs;return row;
    }
    static void position(Frame f,int ms,Instant now,boolean response) {SimulationLearningRules.position(learning(f),ms,now,response);}
    static boolean live(Frame f,Instant now){return SimulationLearningRules.live(learning(f),now);}
    static int score(Frame f){return SimulationLearningRules.score(f.snapshot.scoringTimeline(),f.responseMs);}
    static Session view(Row row,List<Frame> frames,String requestedLicense,Instant now) {
        String license=requestedLicense==null?row.license:SimulationLicensePolicy.require(requestedLicense);
        Frame current=frames.get(row.currentPosition-1);List<Position> positions=new ArrayList<>();
        for(int i=1;i<=10;i++)positions.add(new Position(i,i<=row.completedCount?"COMPLETED":"IN_PROGRESS".equals(row.status)&&i==row.currentPosition?"CURRENT":"NOT_STARTED"));
        boolean restart="IN_PROGRESS".equals(row.status)&&("INTERRUPTED".equals(current.state)||("PLAYING".equals(current.state)&&!live(current,now)));
        return new Session(new Scope(row.user,license,row.bank),row.id,row.mode,row.status,row.bank,row.scoring,policy(row),10,row.completedCount,row.currentPosition,50,
                current.responseMs!=null,current.playbackId,current.state,restart,SimulationLearningRules.POLICY,List.copyOf(positions),row.started,row.completed,row.preset);
    }
    static UUID id(UUID value){if(value==null)throw SimulationExamFailure.invalid("A canonical UUID is required");return value;}
}
