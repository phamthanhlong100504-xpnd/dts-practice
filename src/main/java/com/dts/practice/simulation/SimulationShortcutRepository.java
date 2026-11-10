package com.dts.practice.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.*;
import static com.dts.practice.simulation.SimulationShortcutModel.*;

@Repository
@RequiredArgsConstructor
public class SimulationShortcutRepository {
    private final JdbcTemplate jdbc;private final ObjectMapper json;
    boolean ready(){return Boolean.TRUE.equals(jdbc.queryForObject("SELECT to_regclass('public.simulation_video_shortcut_state') IS NOT NULL AND to_regclass('public.simulation_video_shortcut_events') IS NOT NULL AND EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='simulation_exam_positions' AND column_name='segment_start_ms')",Boolean.class));}
    void lock(UUID user,UUID event){jdbc.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,34))",user+":"+event);}
    Protocol get(String kind,UUID id){return jdbc.query("SELECT * FROM simulation_video_shortcut_state WHERE target_kind=? AND target_id=?",(rs,n)->{
        var p=new Protocol();p.kind=kind;p.target=id;p.user=rs.getObject("user_id",UUID.class);p.secret=rs.getObject("secret_key",UUID.class);p.revision=rs.getLong("revision");
        try {p.skipped=new HashSet<>(Arrays.asList(json.readValue(rs.getString("skipped_positions"),Integer[].class)));p.reasons=new HashMap<>();json.readTree(rs.getString("completion_reasons")).fields().forEachRemaining(e->p.reasons.put(e.getKey(),e.getValue().asText()));}
        catch(Exception e){throw new IllegalStateException("Invalid stored shortcut state");}return p;
    },kind,id).stream().findFirst().orElse(null);}
    void insert(Protocol p,Instant now){jdbc.update("INSERT INTO simulation_video_shortcut_state(target_kind,target_id,user_id,attempt_id,session_id,protocol_version,secret_key,revision,enabled_at) VALUES(?,?,?,?,?,'video-shortcuts-v1',?,1,?)",p.kind,p.target,p.user,"LEARNING".equals(p.kind)?p.target:null,"EXAM".equals(p.kind)?p.target:null,p.secret,Timestamp.from(now));}
    void save(Protocol p){jdbc.update("UPDATE simulation_video_shortcut_state SET revision=?,skipped_positions=?::jsonb,completion_reasons=?::jsonb WHERE target_kind=? AND target_id=? AND user_id=?",p.revision,encode(new TreeSet<>(p.skipped)),encode(new TreeMap<>(p.reasons)),p.kind,p.target,p.user);}
    record Stored(String kind,UUID target,String operation,String hash,Receipt receipt) {}
    Stored event(UUID user,UUID event){return jdbc.query("SELECT target_kind,target_id,operation,request_sha256,receipt_json::text FROM simulation_video_shortcut_events WHERE user_id=? AND event_id=?",(r,n)->new Stored(r.getString(1),r.getObject(2,UUID.class),r.getString(3),r.getString(4),decode(r.getString(5),Receipt.class)),user,event).stream().findFirst().orElse(null);}
    void event(UUID user,String hash,Receipt r){jdbc.update("INSERT INTO simulation_video_shortcut_events(user_id,event_id,target_kind,target_id,operation,request_sha256,receipt_json,created_at) VALUES(?,?,?,?,?,?,?::jsonb,?)",user,r.eventId(),r.targetKind(),r.targetId(),r.operation(),hash,encode(r),Timestamp.from(r.appliedAt()));}
    String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException("Cannot encode shortcut state");}}
    private <T>T decode(String value,Class<T> type){try{return json.readValue(value,type);}catch(Exception e){throw new IllegalStateException("Invalid shortcut receipt");}}
}
