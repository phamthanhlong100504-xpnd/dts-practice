package com.dts.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Published memory tips from the current shared bank")
public record MemoryTipListResponse(
        String bankVersion,
        List<MemoryTipResponse> items,
        long totalElements
) {
}