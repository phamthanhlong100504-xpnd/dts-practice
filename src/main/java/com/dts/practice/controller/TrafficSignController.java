package com.dts.practice.controller;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.TrafficSignListResponse;
import com.dts.practice.dto.response.TrafficSignResponse;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.service.TrafficSignReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/traffic-signs")
@RequiredArgsConstructor
@Tag(
        name = "Traffic signs",
        description = "Published traffic signs for authenticated learners"
)
@SecurityRequirement(name = "BearerAuth")
public class TrafficSignController {

    private final TrafficSignReadService trafficSignReadService;

    @GetMapping
    @Operation(
            summary = "List traffic signs with group filtering, Vietnamese search, and stable pagination"
    )
    public ResponseEntity<ApiResponse<TrafficSignListResponse>> list(
            @Parameter(
                    schema = @Schema(
                            allowableValues = {
                                    "cam",
                                    "nguy_hiem",
                                    "hieu_lenh",
                                    "chi_dan",
                                    "phu"
                            }
                    )
            )
            @RequestParam(
                    name = "group",
                    required = false
            ) String group,

            @Parameter(
                    description = "Search code or name; case and Vietnamese accents are ignored",
                    schema = @Schema(maxLength = 255)
            )
            @RequestParam(
                    name = "q",
                    required = false
            ) String q,

            @Parameter(
                    schema = @Schema(
                            type = "integer",
                            minimum = "0",
                            defaultValue = "0"
                    )
            )
            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            ) String page,

            @Parameter(
                    schema = @Schema(
                            type = "integer",
                            minimum = "1",
                            maximum = "100",
                            defaultValue = "20"
                    )
            )
            @RequestParam(
                    name = "size",
                    defaultValue = "20"
            ) String size
    ) {
        TrafficSignListResponse data = trafficSignReadService.list(
                group,
                q,
                parseInteger(page, "page"),
                parseInteger(size, "size")
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok(data));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a published traffic sign from the current bank"
    )
    public ResponseEntity<ApiResponse<TrafficSignResponse>> get(
            @Parameter(
                    schema = @Schema(
                            type = "string",
                            format = "uuid"
                    )
            )
            @PathVariable("id") String id
    ) {
        UUID signId;

        try {
            signId = UUID.fromString(id);
        } catch (IllegalArgumentException error) {
            throw BusinessException.badRequest(
                    "Traffic sign ID must be a valid UUID"
            );
        }

        if (!signId.toString().equalsIgnoreCase(id)) {
            throw BusinessException.badRequest(
                    "Traffic sign ID must be a canonical UUID"
            );
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(
                        ApiResponse.ok(
                                trafficSignReadService.get(signId)
                        )
                );
    }

    private int parseInteger(String value, String field) {
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException error) {
            throw BusinessException.badRequest(
                    field + " must be a valid integer"
            );
        }
    }
}