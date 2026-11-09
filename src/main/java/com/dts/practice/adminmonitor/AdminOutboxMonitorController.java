package com.dts.practice.adminmonitor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Read-only producer visibility: Kafka ACK NEVER proves Result/Progress projection. */
@RestController
@RequestMapping("/api/v1/exams/admin/integrations")
public class AdminOutboxMonitorController {
    private final ReadModel report;
    public AdminOutboxMonitorController(ReadModel report) { this.report = report; }

    @GetMapping("/outbox")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_integrations:read')")
    public ResponseEntity<Map<String,Object>> outbox() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(report.summary());
    }
    @GetMapping("/outbox/{eventId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_integrations:read')")
    public ResponseEntity<Map<String,Object>> event(@PathVariable UUID eventId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(report.event(eventId));
    }

    @Service
    @Transactional(readOnly=true)
    public static class ReadModel {
        private final JdbcTemplate jdbc;
        // Strict whitelist: no caller-supplied identifiers enter SQL.
        static final List<String> STREAMS = List.of(
            "exam_progress_outbox", "learning_progress_outbox", "practice_result_outbox",
            "practice_exp_outbox", "simulation_performance_outbox");
        public ReadModel(JdbcTemplate jdbc) { this.jdbc=jdbc; }
        public Map<String,Object> summary() {
            List<Map<String,Object>> streams=new ArrayList<>();
            long pending=0,failed=0;
            for(String name:STREAMS) {
                Map<String,Object> counts=jdbc.queryForMap(("""
                    SELECT COUNT(*)::bigint AS total,
                      COUNT(*) FILTER (WHERE published_at IS NULL)::bigint AS pending,
                      COUNT(*) FILTER (WHERE published_at IS NULL AND attempts>0)::bigint AS retrying,
                      MIN(created_at) FILTER (WHERE published_at IS NULL) AS oldest_pending_at,
                      MAX(published_at) AS last_broker_ack_at
                    FROM %s
                    """).formatted(name));
                var entry=new LinkedHashMap<String,Object>();entry.put("stream",name);
                entry.putAll(counts);streams.add(entry);
                pending+=((Number)counts.get("pending")).longValue();
                failed+=((Number)counts.get("retrying")).longValue();
            }
            var out=new LinkedHashMap<String,Object>();
            out.put("sourceService","dts-practice");out.put("asOf",Instant.now());
            out.put("dataStatus",pending>0?"PARTIAL":"READY");
            out.put("sourcePending",pending);out.put("sourceRetrying",failed);
            out.put("streams",streams);
            out.put("consumerReceived",null);out.put("projectionApplied",null);
            out.put("targetApiReady",null);out.put("brokerAckMeansProjectionApplied",false);
            return out;
        }
        public Map<String,Object> event(UUID eventId) {
            if(eventId==null)throw new IllegalArgumentException("eventId required");
            StringBuilder sql=new StringBuilder();
            Object[] params=new Object[STREAMS.size()];
            for(int i=0;i<STREAMS.size();i++) {
                if(i>0)sql.append(" UNION ALL ");
                String name=STREAMS.get(i);
                sql.append("SELECT event_id, '").append(name).append("'::text AS stream,")
                   .append(" attempts,created_at,published_at FROM ").append(name).append(" WHERE event_id=?");
                params[i]=eventId;
            }
            List<Map<String,Object>> found=jdbc.queryForList(sql.toString(),params);
            if(found.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Source event not found");
            if(found.size()!=1)throw new IllegalStateException("Ambiguous producer event identity");
            Map<String,Object> row=found.getFirst();
            var out=new LinkedHashMap<String,Object>();
            out.put("sourceService","dts-practice");out.put("asOf",Instant.now());
            out.put("dataStatus","READY");out.put("eventId",eventId);
            out.put("stream",row.get("stream"));out.put("attempts",row.get("attempts"));
            out.put("createdAt",row.get("created_at"));out.put("brokerAcknowledgedAt",row.get("published_at"));
            out.put("sourceStage",row.get("published_at")==null?"SOURCE_QUEUED":"BROKER_ACKNOWLEDGED");
            out.put("consumerReceived",null);out.put("projectionApplied",null);out.put("targetApiReady",null);
            return out;
        }
    }
}
