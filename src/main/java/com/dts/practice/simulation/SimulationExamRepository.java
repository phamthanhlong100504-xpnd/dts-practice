package com.dts.practice.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import static com.dts.practice.simulation.SimulationExamModel.*;

@Repository
@RequiredArgsConstructor
public class SimulationExamRepository {
    private final JdbcTemplate jdbc;private final ObjectMapper json;
    void lock(UUID user){jdbc.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,32))",user.toString());}
    Optional<Row> created(UUID user,UUID key){return jdbc.query("SELECT * FROM simulation_exam_sessions WHERE user_id=? AND create_key=?",this::row,user,key).stream().findFirst();}
    Optional<Row> active(UUID user){return jdbc.query("SELECT * FROM simulation_exam_sessions WHERE user_id=? AND status IN ('IN_PROGRESS','READY_TO_SUBMIT')",this::row,user).stream().findFirst();}
    Row get(UUID user,UUID id,boolean lock){return jdbc.query("SELECT * FROM simulation_exam_sessions WHERE user_id=? AND id=?"+(lock?" FOR UPDATE":""),this::row,user,id).stream().findFirst().orElseThrow(SimulationExamFailure::missing);}
    List<Frame> frames(UUID id){return jdbc.query("SELECT * FROM simulation_exam_positions WHERE session_id=? ORDER BY position",this::frame,id);}
    Frame frame(UUID id,int position){return jdbc.query("SELECT * FROM simulation_exam_positions WHERE session_id=? AND position=?",this::frame,id,position).stream().findFirst().orElseThrow(()->SimulationExamFailure.invalid("Invalid exam position"));}
    void insert(Row r,List<Frame> frames) {
        if("PRESET".equals(r.mode)) {
            jdbc.update("INSERT INTO simulation_exam_sessions(id,user_id,mode,license_class,bank_version,scoring_version,manifest_sha256,policy_version,passing_score,status,create_key,create_sha256,created_at,started_at,updated_at,preset_set_id,preset_version_id,preset_snapshot) VALUES(?,?,'PRESET',?,?,?,?,?,?,'IN_PROGRESS',?,?,?,?,?,?,?,?::jsonb)",
                    r.id,r.user,r.license,r.bank,r.scoring,r.manifest,r.policyVersion,r.passingScore,r.createKey,r.createHash,time(r.created),time(r.started),time(r.updated),r.preset.presetSetId(),r.preset.presetVersionId(),encode(r.preset));
        } else jdbc.update("INSERT INTO simulation_exam_sessions(id,user_id,mode,license_class,bank_version,scoring_version,manifest_sha256,policy_version,passing_score,status,create_key,create_sha256,created_at,started_at,updated_at) VALUES(?,?,'RANDOM',?,?,?,?,?,?,'IN_PROGRESS',?,?,?,?,?)",
                r.id,r.user,r.license,r.bank,r.scoring,r.manifest,r.policyVersion,r.passingScore,r.createKey,r.createHash,time(r.created),time(r.started),time(r.updated));
        for(Frame f:frames)jdbc.update("INSERT INTO simulation_exam_positions(session_id,position,scenario_id,chapter_name,duration_ms,scenario_snapshot,object_etag,playback_event_id,playback_state) VALUES(?,?,?,?,?,?::jsonb,?,?,'NOT_STARTED')",
                r.id,f.position,f.scenarioId,f.chapterName,f.duration,encode(f.snapshot),f.etag,f.playbackId);
    }
    void save(Row r){jdbc.update("UPDATE simulation_exam_sessions SET status=?,completed_count=?,current_position=?,updated_at=?,completed_at=?,cancelled_at=? WHERE id=? AND user_id=?",r.status,r.completedCount,r.currentPosition,time(r.updated),time(r.completed),time(r.cancelled),r.id,r.user);}
    void save(Frame f){
        var args=new ArrayList<Object>(Arrays.asList(f.playbackId,f.state,time(f.granted),time(f.segmentStarted),time(f.lastObserved),f.lastMs,f.responseMs,f.responseEvent,f.complete,f.score,time(f.completed)));
        if(f.checkpointColumn)args.add(f.segmentStart);args.add(f.session);args.add(f.position);
        jdbc.update("UPDATE simulation_exam_positions SET playback_event_id=?,playback_state=?,granted_at=?,segment_started_at=?,last_observed_at=?,last_media_time_ms=?,first_response_ms=?,first_response_event_id=?,is_completed=?,score=?,completed_at=?"+(f.checkpointColumn?",segment_start_ms=?":"")+" WHERE session_id=? AND position=?",args.toArray());
    }
    record Receipt(UUID session,String kind,String hash,String response) {}
    Optional<Receipt> receipt(UUID user,UUID event){return jdbc.query("SELECT session_id,kind,request_sha256,response_json::text FROM simulation_exam_events WHERE user_id=? AND event_id=?",(r,n)->new Receipt(r.getObject(1,UUID.class),r.getString(2),r.getString(3),r.getString(4)),user,event).stream().findFirst();}
    void receipt(UUID user,UUID event,UUID session,String kind,String hash,Session view,Instant now){jdbc.update("INSERT INTO simulation_exam_events(user_id,event_id,session_id,kind,request_sha256,response_json,created_at) VALUES(?,?,?,?,?,?::jsonb,?)",user,event,session,kind,hash,encode(view),time(now));}
    void result(Row r,Result result){jdbc.update("INSERT INTO simulation_exam_results(session_id,total_score,passing_score,policy_version,passed,result_status,result_json,completed_at) VALUES(?,?,?,?,?,?,?::jsonb,?)",r.id,result.totalScore(),r.passingScore,r.policyVersion,result.passed(),result.resultStatus(),encode(result),time(r.completed));}
    Result result(UUID session){return jdbc.query("SELECT result_json::text FROM simulation_exam_results WHERE session_id=?",(r,n)->decode(r.getString(1),Result.class),session).stream().findFirst().orElseThrow(()->SimulationExamFailure.conflict("NOT_COMPLETED","Submit all ten completed videos first"));}
    String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException("Cannot encode simulation exam data");}}
    <T>T decode(String value,Class<T> type){try{return json.readValue(value,type);}catch(Exception e){throw new IllegalStateException("Invalid stored simulation exam data");}}
    record Latest(UUID sessionId,int score,boolean passed,Instant completedAt) {}
    Map<UUID,Latest> latestPresets(UUID user) {
        var result=new HashMap<UUID,Latest>();
        jdbc.query("SELECT DISTINCT ON (s.preset_version_id) s.preset_version_id,s.id,r.total_score,r.passed,s.completed_at FROM simulation_exam_sessions s JOIN simulation_exam_results r ON r.session_id=s.id WHERE s.user_id=? AND s.mode='PRESET' AND s.status='COMPLETED' ORDER BY s.preset_version_id,s.completed_at DESC,s.id DESC",
                rs->{result.put(rs.getObject(1,UUID.class),new Latest(rs.getObject(2,UUID.class),rs.getInt(3),rs.getBoolean(4),rs.getTimestamp(5).toInstant()));},user);
        return Map.copyOf(result);
    }
    private Row row(ResultSet s,int index)throws SQLException {
        var r=new Row();r.id=s.getObject("id",UUID.class);r.user=s.getObject("user_id",UUID.class);r.createKey=s.getObject("create_key",UUID.class);r.createHash=s.getString("create_sha256");
        r.mode=s.getString("mode");r.preset="PRESET".equals(r.mode)?decode(s.getString("preset_snapshot"),SimulationPresets.Ref.class):null;
        r.license=s.getString("license_class");r.bank=s.getString("bank_version");r.scoring=s.getString("scoring_version");r.manifest=s.getString("manifest_sha256");r.policyVersion=s.getString("policy_version");r.passingScore=s.getInt("passing_score");r.status=s.getString("status");r.completedCount=s.getInt("completed_count");r.currentPosition=s.getInt("current_position");
        r.created=instant(s,"created_at");r.started=instant(s,"started_at");r.updated=instant(s,"updated_at");r.completed=instant(s,"completed_at");r.cancelled=instant(s,"cancelled_at");return r;
    }
    private Frame frame(ResultSet s,int index)throws SQLException {
        var f=new Frame();f.session=s.getObject("session_id",UUID.class);f.position=s.getInt("position");f.scenarioId=s.getObject("scenario_id",UUID.class);f.chapterName=s.getString("chapter_name");f.duration=s.getInt("duration_ms");f.snapshot=decode(s.getString("scenario_snapshot"),SimulationBank.Scenario.class);f.etag=s.getString("object_etag");
        for(int c=1;c<=s.getMetaData().getColumnCount();c++)if("segment_start_ms".equalsIgnoreCase(s.getMetaData().getColumnLabel(c))){f.segmentStart=s.getInt(c);f.checkpointColumn=true;break;}
        f.playbackId=s.getObject("playback_event_id",UUID.class);f.state=s.getString("playback_state");f.lastMs=s.getInt("last_media_time_ms");f.responseMs=(Integer)s.getObject("first_response_ms");f.responseEvent=s.getObject("first_response_event_id",UUID.class);f.score=(Integer)s.getObject("score");f.complete=s.getBoolean("is_completed");
        f.granted=instant(s,"granted_at");f.segmentStarted=instant(s,"segment_started_at");f.lastObserved=instant(s,"last_observed_at");f.completed=instant(s,"completed_at");return f;
    }
    private static Timestamp time(Instant v){return v==null?null:Timestamp.from(v);}
    private static Instant instant(ResultSet r,String column)throws SQLException{var t=r.getTimestamp(column);return t==null?null:t.toInstant();}
}
