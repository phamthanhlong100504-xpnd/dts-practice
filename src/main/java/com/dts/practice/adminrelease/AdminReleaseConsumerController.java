package com.dts.practice.adminrelease;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Internal-only endpoint. HMAC is required even though web security permits service requests. */
@RestController
@RequestMapping("/api/v1/questions/internal/admin-releases/simulation")
public class AdminReleaseConsumerController {
    private final AdminReleaseAuth auth;
    private final AdminReleaseConsumerService service;
    private final ObjectMapper json;
    public AdminReleaseConsumerController(AdminReleaseAuth auth,AdminReleaseConsumerService service,ObjectMapper json){
        this.auth=auth;this.service=service;this.json=json;
    }
    @PostMapping("/prepare")
    public ResponseEntity<?> prepare(@RequestHeader(value="X-DTS-Admin-Release-Time",required=false) String at,
                                     @RequestHeader(value="X-DTS-Admin-Release-Signature",required=false) String sign,
                                     @RequestBody String raw) {
        String path="/api/v1/questions/internal/admin-releases/simulation/prepare";
        try {
            auth.require("POST",path,at,sign,raw);
            var request=json.readValue(raw,AdminReleaseConsumerService.Prepare.class);
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.prepare(request));
        } catch(SecurityException ex){return ResponseEntity.status(HttpStatus.FORBIDDEN).cacheControl(CacheControl.noStore()).build();}
        catch(IllegalStateException ex){return ResponseEntity.status(HttpStatus.CONFLICT).cacheControl(CacheControl.noStore()).build();}
        catch(Exception ex){return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noStore()).build();}
    }
    @GetMapping("/{releaseId}")
    public ResponseEntity<?> status(@PathVariable UUID releaseId,
                                     @RequestHeader(value="X-DTS-Admin-Release-Time",required=false) String at,
                                     @RequestHeader(value="X-DTS-Admin-Release-Signature",required=false) String sign) {
        String path="/api/v1/questions/internal/admin-releases/simulation/"+releaseId;
        try {
            auth.require("GET",path,at,sign,"");
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.status(releaseId));
        } catch(SecurityException ex){return ResponseEntity.status(HttpStatus.FORBIDDEN).cacheControl(CacheControl.noStore()).build();}
        catch(Exception ex){return ResponseEntity.status(HttpStatus.NOT_FOUND).cacheControl(CacheControl.noStore()).build();}
    }
}
