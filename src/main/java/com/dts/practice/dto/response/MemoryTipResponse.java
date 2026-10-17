package com.dts.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Published memory tip for the learner accordion")
public record MemoryTipResponse(

        @Schema(description = "Stable UUID from the existing memory_tips record")
        UUID id,

        Integer sourceTipId,
        Integer sortOrder,
        String title,
        String quickRule,
        String explanation
) {
}