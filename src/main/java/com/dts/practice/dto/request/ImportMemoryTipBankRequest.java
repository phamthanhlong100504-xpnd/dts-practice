
package com.dts.practice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ImportMemoryTipBankRequest(

        @NotBlank
        @Pattern(regexp = "dts-2026-600-v1")
        String bankVersion,

        @NotEmpty
        @Size(min = 12, max = 12)
        List<@Valid BankMemoryTip> tips

) {

    public record BankMemoryTip(

            @NotNull
            UUID contentBuilderMemoryTipId,

            @NotNull
            @Min(1)
            @Max(12)
            Integer sourceTipId,

            @NotNull
            @Min(1)
            @Max(12)
            Integer sortOrder,

            @NotBlank
            @Size(max = 255)
            String title,

            @NotBlank
            String quickRule,

            @NotBlank
            String explanation,

            @NotBlank
            @Pattern(regexp = "PUBLISHED")
            String status,

            @NotNull
            LocalDateTime publishedAt

    ) {
    }
}
