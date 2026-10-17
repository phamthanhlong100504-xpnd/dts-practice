package com.dts.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TrafficSignListResponse(
        String bankVersion,
        List<TrafficSignResponse> items,
        long totalElements,
        int page,
        int size,
        int totalPages,

        @Schema(description = "Counts for all five groups in the current published bank, before group and q filters")
        List<Group> groups
) {
    public record Group(
            String groupCode,
            String groupName,
            long totalElements
    ) {
    }
}