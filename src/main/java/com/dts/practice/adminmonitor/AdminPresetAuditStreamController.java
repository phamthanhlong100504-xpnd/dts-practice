package com.dts.practice.adminmonitor;

import com.dts.practice.exception.BusinessException;
import com.dts.practice.dto.response.ErrorResponse;
import java.sql.Timestamp;
import java.time.Duration;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** BE06-08: source-owned, read-only, paged audit of practice exam-set administration. */
@RestController
@RequestMapping("/api/v1/exams/admin/audit")
public class AdminPresetAuditStreamController {
    private final AuditReader reader;
    public AdminPresetAuditStreamController(AuditReader reader) { this.reader = reader; }

    @GetMapping("/preset-sets")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_audit:read')")
    public ResponseEntity<Map<String,Object>> list(
            @RequestParam(required=false) Instant from,
            @RequestParam(required=false) Instant to,
            @RequestParam(required=false) UUID actorId,
            @RequestParam(required=false) String action,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="30") int size) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(reader.list(from,to,actorId,action,page,size));
    }

    // Practice's legacy global Exception handler treats invalid query types as HTTP 500.
    // Scope this HTTP 400 contract to the new audit endpoint without changing other APIs.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> invalidQueryType(MethodArgumentTypeMismatchException ignored) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noStore())
            .body(ErrorResponse.of("BAD_REQUEST", "Invalid audit query parameter", (String)null));
    }

    @Service
    @Transactional(readOnly=true)
    public static class AuditReader {
        private final JdbcTemplate jdbc;
        public AuditReader(JdbcTemplate jdbc) { this.jdbc = jdbc; }

        // Only event metadata is selected. Details JSON and free-text reasons are not exposed:
        // they may contain credential-like values and have no server-enforced redaction contract.
        private static final String SQL = """
            WITH entries AS (
                SELECT id::text AS "auditId", actor_id AS "actorId", action,
                    'practice-exam-set-draft'::text AS "resourceType",
                    draft_id::text AS "resourceId", NULL::text AS "versionId",
                    occurred_at AS "occurredAt", 'RECORDED'::text AS result,
                    FALSE AS "reasonPresent", 'admin_preset_draft_audit'::text AS "sourceJournal"
                FROM admin_preset_draft_audit
                WHERE occurred_at >= ? AND occurred_at < ?
                UNION ALL
                SELECT id::text AS "auditId", actor_id AS "actorId", action,
                    'practice-exam-set-release'::text AS "resourceType",
                    COALESCE(draft_id::text,version_id::text) AS "resourceId",
                    version_id::text AS "versionId", occurred_at AS "occurredAt",
                    'RECORDED'::text AS result, (reason IS NOT NULL) AS "reasonPresent",
                    'admin_preset_release_audit'::text AS "sourceJournal"
                FROM admin_preset_release_audit
                WHERE occurred_at >= ? AND occurred_at < ?
            )
            SELECT "auditId","actorId",action,"resourceType","resourceId","versionId",
                "occurredAt",result,"reasonPresent","sourceJournal"
            FROM entries
            WHERE (?::uuid IS NULL OR "actorId"=?::uuid)
              AND (?::text IS NULL OR action=?)
            ORDER BY "occurredAt" DESC,"auditId" DESC
            LIMIT ? OFFSET ?
            """;

        public Map<String,Object> list(Instant from, Instant to, UUID actorId,
                                       String action, int page, int size) {
            Instant now = Instant.now();
            Instant end = to == null ? now : to;
            Instant start = from == null ? end.minus(Duration.ofDays(30)) : from;
            if (page < 0 || page > 1000 || size < 1 || size > 100
                    || !start.isBefore(end) || end.isAfter(now.plusSeconds(60))
                    || Duration.between(start,end).compareTo(Duration.ofDays(90)) > 0) {
                throw BusinessException.badRequest("Invalid audit interval or pagination");
            }
            if (action != null && !action.matches("[A-Z_]{1,24}")) {
                throw BusinessException.badRequest("Invalid audit action");
            }
            List<Map<String,Object>> rows = jdbc.queryForList(SQL,
                Timestamp.from(start),Timestamp.from(end),Timestamp.from(start),Timestamp.from(end),
                actorId,actorId,action,action,size+1,(long)page*size);
            boolean hasMore = rows.size() > size;
            List<Map<String,Object>> items = new ArrayList<>(rows.subList(0,Math.min(size,rows.size())));
            var out = new LinkedHashMap<String,Object>();
            out.put("sourceService","dts-practice");
            out.put("dataStatus","READY");
            out.put("asOf",now);out.put("from",start);out.put("to",end);
            out.put("timezone","Asia/Ho_Chi_Minh");
            out.put("page",page);out.put("size",size);out.put("hasMore",hasMore);
            out.put("items",items);
            out.put("coverage",List.of("admin_preset_draft_audit","admin_preset_release_audit"));
            out.put("warnings",List.of("Free-text reasons and JSON details are deliberately withheld",
                "RECORDED means a journal event exists, not that a downstream projection was applied"));
            return out;
        }
    }
}
