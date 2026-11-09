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

import static com.dts.practice.simulation.SimulationReviewModel.*;

@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Simulation review and read-only replay")
public class SimulationReviewController {
    private final SimulationReviewService reviews;
    private final SimulationReplayService replays;
    private static final String READ = "hasAuthority('PERM_simulations:read')";
    private static final String MEDIA = READ + " and hasAuthority('PERM_media:read')";

    @GetMapping("/review-needed/overview")
    @PreAuthorize(READ)
    @Operation(summary = "Read distinct practiced and global need-review counts for the published bank")
    public ResponseEntity<ApiResponse<ReviewOverview>> overview(
            @RequestParam(required = false) String licenseClass,
            @RequestParam(required = false) String bankVersion) {
        return ok(reviews.overview(licenseClass, bankVersion));
    }

    @GetMapping("/review-needed")
    @PreAuthorize(MEDIA)
    @Operation(summary = "Read completed scenarios whose compatible best score is below five")
    public ResponseEntity<ApiResponse<ReviewPage>> list(
            @RequestParam(required = false) String licenseClass,
            @RequestParam(required = false) String bankVersion,
            @RequestParam(required = false) Integer chapterId,
            @RequestParam(defaultValue = "BEST_SCORE_ASC") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String expectedDatasetVersion) {
        return ok(reviews.list(licenseClass, bankVersion, chapterId, sort, page, size, expectedDatasetVersion));
    }

    @GetMapping("/attempts/{attemptId}/replay")
    @PreAuthorize(MEDIA)
    @Operation(summary = "Resolve a fresh private video URL without modifying a completed attempt")
    public ResponseEntity<ApiResponse<Replay>> replay(@PathVariable String attemptId) {
        return ok(replays.replay(SimulationLearningController.uuid(attemptId)));
    }

    private static <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(value));
    }
}
