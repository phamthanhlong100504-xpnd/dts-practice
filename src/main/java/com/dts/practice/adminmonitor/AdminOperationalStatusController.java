package com.dts.practice.adminmonitor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** BE06-09: owner-scoped, no-store operational evidence. No cross-service success is inferred. */
@RestController
public class AdminOperationalStatusController {
    private final AdminOutboxMonitorController.ReadModel monitor;
    public AdminOperationalStatusController(AdminOutboxMonitorController.ReadModel monitor) { this.monitor=monitor; }

    @GetMapping("/api/v1/exams/admin/operations/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('PERM_system-status:read')")
    public ResponseEntity<Map<String,Object>> status() {

        Map<String,Object> source=monitor.summary();
        var out=new LinkedHashMap<String,Object>();out.put("sourceService","dts-practice");out.put("asOf",Instant.now());
        out.put("dataStatus","PARTIAL");out.put("localOutboxStatus",source.get("dataStatus"));out.put("outbox",source);
        out.put("resultConsumerReadiness",null);out.put("progressConsumerReadiness",null);
        out.put("missingSources",List.of("dts-result", "dts-progress"));
        out.put("warnings",List.of("Broker ACK is not projection confirmation; this endpoint observes producer outboxes only"));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out);
    }
}
