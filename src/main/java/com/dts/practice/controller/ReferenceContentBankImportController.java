
package com.dts.practice.controller;

import com.dts.practice.dto.request.ImportMemoryTipBankRequest;
import com.dts.practice.dto.request.ImportTrafficSignBankRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.service.MemoryTipBankImportService;
import com.dts.practice.service.TrafficSignBankImportService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reference-content/admin")
@RequiredArgsConstructor
@Tag(name = "Reference content bank import")
@SecurityRequirement(name = "BearerAuth")
public class ReferenceContentBankImportController {

    private final TrafficSignBankImportService
            trafficSignBankImportService;

    private final MemoryTipBankImportService
            memoryTipBankImportService;


    @PostMapping("/import-traffic-signs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> importTrafficSigns(
            @Valid @RequestBody
            ImportTrafficSignBankRequest request
    ) {

        int imported =
                trafficSignBankImportService.importBank(request);

        return ApiResponse.ok(
                Map.of(
                        "bankVersion", request.bankVersion(),
                        "imported", imported
                )
        );
    }

    @PostMapping("/import-memory-tips")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> importMemoryTips(
            @Valid @RequestBody
            ImportMemoryTipBankRequest request
    ) {

        int imported =
                memoryTipBankImportService.importBank(request);

        return ApiResponse.ok(
                Map.of(
                        "bankVersion", request.bankVersion(),
                        "imported", imported
                )
        );
    }
}
