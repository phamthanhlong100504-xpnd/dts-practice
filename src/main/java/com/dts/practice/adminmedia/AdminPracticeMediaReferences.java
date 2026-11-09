package com.dts.practice.adminmedia;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Read-only discovery for Media references retained by Practice.
 * Do not interpret this report as permission to retire an asset: Examination,
 * Media and other consumers are NOT verified here.
 */
@Service
public class AdminPracticeMediaReferences {
    private static final int MAX_PER_SOURCE = 500;
    private final JdbcTemplate jdbc;

    public AdminPracticeMediaReferences(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static void requireAdminRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))
                || auth.getAuthorities().stream().noneMatch(a -> "PERM_media:read".equals(a.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "PRACTICE_MEDIA_REFERENCE_READ_FORBIDDEN");
        }
    }

    @Transactional(readOnly = true)
    public Report inspect(UUID mediaId) {
        requireAdminRead();
        if (mediaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MEDIA_ID_REQUIRED");
        }
        List<Item> references = new ArrayList<>();
        boolean truncated = false;
        String id = mediaId.toString();

        // Preserve every imported bank version, even if no longer current.
        truncated |= add(references, jdbc.query("""
            SELECT q.id::text AS resource_id, q.bank_version AS version_id,
                   'IMPORTED'::text AS reference_status
              FROM questions q
             WHERE q.media_file_id = ?
             ORDER BY q.bank_version, q.id
             LIMIT 501
            """, (rs, n) -> new Item("questions", rs.getString("resource_id"),
                rs.getString("version_id"), "QUESTION_MEDIA", rs.getString("reference_status")), mediaId));

        truncated |= add(references, jdbc.query("""
            SELECT t.id::text AS resource_id, t.bank_version AS version_id,
                   t.status::text AS reference_status
              FROM traffic_signs t
             WHERE t.media_file_id = ?
             ORDER BY t.bank_version, t.id
             LIMIT 501
            """, (rs, n) -> new Item("traffic-signs", rs.getString("resource_id"),
                rs.getString("version_id"), "TRAFFIC_SIGN_IMAGE", rs.getString("reference_status")), mediaId));

        // Snapshot JSON is immutable for completed/cancelled sessions. Search by text,
        // not a UUID cast: malformed historical JSON must not break discovery.
        truncated |= add(references, jdbc.query("""
            SELECT a.id::text AS resource_id, a.bank_version AS version_id,
                   a.status::text AS reference_status
              FROM simulation_attempts a
             WHERE a.scenario_snapshot->>'videoMediaId' = ?
             ORDER BY a.id
             LIMIT 501
            """, (rs, n) -> new Item("simulation-attempts", rs.getString("resource_id"),
                rs.getString("version_id"), "SIMULATION_VIDEO", rs.getString("reference_status")), id));

        truncated |= add(references, jdbc.query("""
            SELECT a.id::text AS resource_id, a.bank_version AS version_id,
                   a.status::text AS reference_status
              FROM simulation_attempts a
             WHERE a.scenario_snapshot->>'thumbnailMediaId' = ?
             ORDER BY a.id
             LIMIT 501
            """, (rs, n) -> new Item("simulation-attempts", rs.getString("resource_id"),
                rs.getString("version_id"), "SIMULATION_THUMBNAIL", rs.getString("reference_status")), id));

        truncated |= add(references, jdbc.query("""
            SELECT p.session_id::text || ':' || p.position::text AS resource_id,
                   s.bank_version AS version_id, s.status::text AS reference_status
              FROM simulation_exam_positions p
              JOIN simulation_exam_sessions s ON s.id = p.session_id
             WHERE p.scenario_snapshot->>'videoMediaId' = ?
             ORDER BY p.session_id, p.position
             LIMIT 501
            """, (rs, n) -> new Item("simulation-exam-positions", rs.getString("resource_id"),
                rs.getString("version_id"), "SIMULATION_VIDEO", rs.getString("reference_status")), id));

        truncated |= add(references, jdbc.query("""
            SELECT p.session_id::text || ':' || p.position::text AS resource_id,
                   s.bank_version AS version_id, s.status::text AS reference_status
              FROM simulation_exam_positions p
              JOIN simulation_exam_sessions s ON s.id = p.session_id
             WHERE p.scenario_snapshot->>'thumbnailMediaId' = ?
             ORDER BY p.session_id, p.position
             LIMIT 501
            """, (rs, n) -> new Item("simulation-exam-positions", rs.getString("resource_id"),
                rs.getString("version_id"), "SIMULATION_THUMBNAIL", rs.getString("reference_status")), id));

        return new Report(mediaId, "PRACTICE", false,
            "OTHER_SERVICE_REFERENCES_NOT_VERIFIED", references.size(),
            truncated, false, List.copyOf(references));
    }

    private static boolean add(List<Item> target, List<Item> source) {
        int included = Math.min(source.size(), MAX_PER_SOURCE);
        target.addAll(source.subList(0, included));
        return source.size() > MAX_PER_SOURCE;
    }

    public record Item(String resourceType, String resourceId, String versionId,
                       String usageType, String referenceStatus) {}
    public record Report(UUID mediaId, String source, boolean verified,
                         String reasonCode, int knownReferenceCount,
                         boolean truncated, boolean canRetire, List<Item> references) {}
}
