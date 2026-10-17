package com.dts.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Published traffic sign for the student list and detail drawer")
public record TrafficSignResponse(
        UUID id,
        String bankVersion,
        String code,
        String name,
        String groupCode,
        String groupName,

        @Schema(description = "Media UUID. Resolve through GET /api/v1/media/{mediaId} or the existing MediaImage component.")
        UUID mediaFileId,

        @Schema(description = "Stable order from the imported sourceSignId")
        Integer sortOrder,

        String meaning,

        @Schema(description = "Imported quickTip; null when no content is available")
        String memoryTip
) {
}