package com.dts.practice.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import static com.dts.practice.simulation.SimulationLearningModel.*;

@Repository
@RequiredArgsConstructor
public class SimulationLearningRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    void eventLock(UUID user,UUID event) {jdbc.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,31))",user+":"+event);}
    record Receipt(UUID attemptId,String kind,String digest) {}
    Optional<Receipt> receipt(UUID user,UUID event) {
        return jdbc.query("SELECT attempt_id,kind,request_sha256 FROM simulation_attempt_events WHERE user_id=? AND event_id=?",
                (r,n)->new Receipt(r.getObject(1,UUID.class),r.getString(2),r.getString(3)),user,event).stream().findFirst();
    }
    void receipt(UUID user,UUID event,UUID attempt,String kind,String hash,Instant now) {
        jdbc.update("INSERT INTO simulation_attempt_events(user_id,event_id,attempt_id,kind,request_sha256,created_at) VALUES(?,?,?,?,?,?)",user,event,attempt,kind,hash,time(now));
    }
    Row get(UUID user,UUID id,boolean lock) {
        return jdbc.query("SELECT * FROM simulation_attempts WHERE user_id=? AND id=?"+(lock?" FOR UPDATE":""),this::row,user,id)
                .stream().findFirst().orElseThrow(SimulationFailure::notFound);
    }
    Optional<Row> latest(UUID user,String bank,UUID scenario) {
        return jdbc.query("SELECT * FROM simulation_attempts WHERE user_id=? AND bank_version=? AND scenario_id=? ORDER BY attempt_sequence DESC LIMIT 1",this::row,user,bank,scenario).stream().findFirst();
    }
    boolean playing(UUID user,String bank,UUID scenario) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM simulation_attempts WHERE user_id=? AND bank_version=? AND scenario_id=? AND status='PLAYING')",Boolean.class,user,bank,scenario));
    }
    void insert(Row r) {
        jdbc.update("""
            INSERT INTO simulation_attempts(id,user_id,create_event_id,bank_version,scoring_version,manifest_sha256,
                scenario_id,scenario_number,chapter_id,license_class,duration_ms,scenario_snapshot,next_scenario_id,status,playback_event_id,created_at,updated_at)
            VALUES(?,?,?,?,?,?,?,?,?,?,?,?::jsonb,?,'CREATED',?,?,?)
            """,r.id,r.user,r.createEvent,r.bank,r.scoring,r.manifest,r.scenarioId,r.number,r.chapter,r.license,r.duration,encode(r.snapshot),r.nextScenario,r.playbackId,time(r.created),time(r.updated));
    }
    void save(Row r) {
        jdbc.update("""
            UPDATE simulation_attempts SET status=?,playback_event_id=?,granted_at=?,started_at=?,segment_started_at=?,segment_start_ms=?,
              last_media_time_ms=?,last_observed_at=?,first_response_ms=?,first_response_event_id=?,score=?,completed_at=?,interrupted_reason=?,updated_at=?
            WHERE user_id=? AND id=?
            """,r.status,r.playbackId,time(r.granted),time(r.started),time(r.segmentStarted),r.segmentStart,r.lastMs,time(r.lastObserved),r.responseMs,r.responseEvent,
                r.score,time(r.completed),r.reason,time(r.updated),r.user,r.id);
    }
    Map<UUID,Stats> stats(UUID user,String bank) {
        var result=new HashMap<UUID,Stats>();
        jdbc.query("SELECT scenario_id,count(*) AS total,max(score) AS best FROM simulation_attempts WHERE user_id=? AND bank_version=? AND status='COMPLETED' GROUP BY scenario_id",
                (org.springframework.jdbc.core.RowCallbackHandler) r->result.put(r.getObject(1,UUID.class),new Stats(r.getInt(2),r.getInt(3),false)),user,bank);
        jdbc.query("SELECT scenario_id FROM simulation_bookmarks WHERE user_id=? AND bank_version=?",(org.springframework.jdbc.core.RowCallbackHandler) r->{
            UUID id=r.getObject(1,UUID.class);var old=result.getOrDefault(id,new Stats(0,null,false));result.put(id,new Stats(old.completedAttemptCount(),old.bestScore(),true));
        },user,bank);
        return result;
    }
    void bookmark(UUID user,String bank,UUID scenario,boolean value,Instant now) {
        if(value) jdbc.update("INSERT INTO simulation_bookmarks(user_id,bank_version,scenario_id,updated_at) VALUES(?,?,?,?) ON CONFLICT(user_id,bank_version,scenario_id) DO NOTHING",user,bank,scenario,time(now));
        else jdbc.update("DELETE FROM simulation_bookmarks WHERE user_id=? AND bank_version=? AND scenario_id=?",user,bank,scenario);
    }
    Cursor cursor(UUID user,String bank,int chapter) {
        return jdbc.query("SELECT scenario_id,source_attempt_id,revision FROM simulation_cursors WHERE user_id=? AND bank_version=? AND chapter_id=?",
                (r,n)->new Cursor(r.getObject(1,UUID.class),r.getObject(2,UUID.class),r.getLong(3)),user,bank,chapter).stream().findFirst().orElse(new Cursor(null,null,0));
    }
    void cursorStart(Row r,Instant now) {
        jdbc.update("""
            INSERT INTO simulation_cursors(user_id,bank_version,chapter_id,scenario_id,source_attempt_id,revision,updated_at) VALUES(?,?,?,?,?,1,?)
            ON CONFLICT(user_id,bank_version,chapter_id) DO UPDATE SET scenario_id=excluded.scenario_id,source_attempt_id=excluded.source_attempt_id,
              revision=simulation_cursors.revision+1,updated_at=excluded.updated_at
            """,r.user,r.bank,r.chapter,r.scenarioId,r.id,time(now));
    }
    void cursorComplete(Row r,Instant now) {
        jdbc.update("UPDATE simulation_cursors SET scenario_id=?,revision=revision+1,updated_at=? WHERE user_id=? AND bank_version=? AND chapter_id=? AND source_attempt_id=?",
                r.nextScenario==null?r.scenarioId:r.nextScenario,time(now),r.user,r.bank,r.chapter,r.id);
    }
    boolean cursorSet(UUID user,String bank,int chapter,UUID scenario,long expected,Instant now) {
        if(expected==0) return jdbc.update("INSERT INTO simulation_cursors(user_id,bank_version,chapter_id,scenario_id,revision,updated_at) VALUES(?,?,?,?,1,?) ON CONFLICT DO NOTHING",user,bank,chapter,scenario,time(now))==1;
        return jdbc.update("UPDATE simulation_cursors SET scenario_id=?,source_attempt_id=NULL,revision=revision+1,updated_at=? WHERE user_id=? AND bank_version=? AND chapter_id=? AND revision=?",scenario,time(now),user,bank,chapter,expected)==1;
    }
    String encode(Object value) {try {return json.writeValueAsString(value);} catch(Exception e) {throw new IllegalStateException("Cannot encode simulation snapshot");}}
    private Row row(ResultSet s,int index) throws SQLException {
        var r=new Row();r.id=s.getObject("id",UUID.class);r.user=s.getObject("user_id",UUID.class);r.createEvent=s.getObject("create_event_id",UUID.class);
        r.bank=s.getString("bank_version");r.scoring=s.getString("scoring_version");r.manifest=s.getString("manifest_sha256");r.scenarioId=s.getObject("scenario_id",UUID.class);
        r.number=s.getInt("scenario_number");r.chapter=s.getInt("chapter_id");r.license=s.getString("license_class");r.duration=s.getInt("duration_ms");
        r.nextScenario=s.getObject("next_scenario_id",UUID.class);r.status=s.getString("status");r.playbackId=s.getObject("playback_event_id",UUID.class);
        r.segmentStart=s.getInt("segment_start_ms");r.lastMs=s.getInt("last_media_time_ms");r.responseMs=(Integer)s.getObject("first_response_ms");r.score=(Integer)s.getObject("score");
        r.responseEvent=s.getObject("first_response_event_id",UUID.class);r.reason=s.getString("interrupted_reason");
        r.granted=instant(s,"granted_at");r.started=instant(s,"started_at");r.segmentStarted=instant(s,"segment_started_at");r.lastObserved=instant(s,"last_observed_at");
        r.created=instant(s,"created_at");r.updated=instant(s,"updated_at");r.completed=instant(s,"completed_at");
        try {r.snapshot=json.readValue(s.getString("scenario_snapshot"),SimulationBank.Scenario.class);} catch(Exception e) {throw new SQLException("Invalid stored simulation snapshot");}return r;
    }
    private static Timestamp time(Instant value) {return value==null?null:Timestamp.from(value);}
    private static Instant instant(ResultSet r,String name) throws SQLException {var value=r.getTimestamp(name);return value==null?null:value.toInstant();}
}
