package com.dts.practice.controller;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.MemoryTipListResponse;
import com.dts.practice.service.MemoryTipReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/memory-tips")
@RequiredArgsConstructor
@Tag(
        name = "Memory tips",
        description = "Published memory tips for authenticated learners"
)
@SecurityRequirement(name = "BearerAuth")
public class MemoryTipController {

    private final MemoryTipReadService memoryTipReadService;

    @GetMapping
    @Operation(
            summary = "List all published memory tips from the current shared bank",
            description = "Returns all published tips in stable sort order. The bank is shared across license classes."
    )
    public ResponseEntity<ApiResponse<MemoryTipListResponse>> list() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok(memoryTipReadService.list()));
    }
}