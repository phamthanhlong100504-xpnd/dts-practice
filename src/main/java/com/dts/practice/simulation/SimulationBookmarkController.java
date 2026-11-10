package com.dts.practice.simulation;

import com.dts.practice.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.dts.practice.simulation.SimulationBookmarkModel.*;

@RestController
@RequestMapping("/api/v1/simulations/bookmarks")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Simulation bookmarks")
public class SimulationBookmarkController {
    private final SimulationBookmarkService bookmarks;

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('PERM_simulations:read')")
    @Operation(summary = "Read all published simulation bookmark counts independent of practice scores")
    public ResponseEntity<ApiResponse<BookmarkOverview>> overview(
            @RequestParam(required = false) String licenseClass,
            @RequestParam(required = false) String bankVersion) {
        return ok(bookmarks.overview(licenseClass, bankVersion));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_simulations:read') and hasAuthority('PERM_media:read')")
    @Operation(summary = "Read all bookmarked published scenarios including unpracticed and perfect results")
    public ResponseEntity<ApiResponse<BookmarkPage>> list(
            @RequestParam(required = false) String licenseClass,
            @RequestParam(required = false) String bankVersion,
            @RequestParam(required = false) Integer chapterId,
            @RequestParam(defaultValue = "SCENARIO_ASC") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String expectedDatasetVersion) {
        return ok(bookmarks.list(licenseClass, bankVersion, chapterId, sort, page, size, expectedDatasetVersion));
    }

    private static <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(value));
    }
}
