
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

public record ImportTrafficSignBankRequest(

        @NotBlank
        @Pattern(regexp = "dts-traffic-signs-v1")
        String bankVersion,

        @NotEmpty
        @Size(min = 221, max = 221)
        List<@Valid BankTrafficSign> signs

) {

    public record BankTrafficSign(

            @NotNull
            UUID contentBuilderTrafficSignId,

            @NotNull
            @Min(1)
            @Max(221)
            Integer sourceSignId,

            @NotBlank
            @Size(max = 32)
            String code,

            @NotBlank
            @Size(max = 255)
            String name,

            @NotBlank
            @Pattern(
                    regexp = "cam|nguy_hiem|hieu_lenh|chi_dan|phu"
            )
            String category,

            @NotBlank
            @Size(max = 100)
            String categoryName,

            @NotBlank
            String meaning,

            @NotBlank
            String quickTip,

            @NotBlank
            @Size(max = 255)
            String sourceImage,

            @NotNull
            UUID mediaFileId,

            @NotEmpty
            List<@NotBlank String> applicableLicenses,

            @NotBlank
            @Pattern(regexp = "PUBLISHED")
            String status,

            @NotNull
            LocalDateTime publishedAt

    ) {
    }
}
