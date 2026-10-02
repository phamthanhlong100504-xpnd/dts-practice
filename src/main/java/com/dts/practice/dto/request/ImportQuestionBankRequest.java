package com.dts.practice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ImportQuestionBankRequest(
        @NotBlank String bankVersion,
        @NotEmpty @Size(min = 600, max = 600)
        List<@Valid BankQuestion> questions
) {
    public record BankQuestion(
            @NotNull UUID contentBuilderQuestionId,
            @NotNull @Min(1) @Max(600) Integer sourceQuestionId,
            @NotNull @Min(1) @Max(6) Integer chapter,
            @NotBlank String questionText,
            @NotEmpty @Size(min = 2, max = 4)
            List<@Valid BankOption> options,
            @NotBlank @Pattern(regexp = "[A-D]") String correctAnswer,
            @NotNull Boolean isCritical,
            @NotEmpty List<@NotBlank String> applicableLicenses,
            @NotNull List<@NotBlank String> criticalLicenses,
            String explanation,
            UUID mediaFileId
    ) {}

    public record BankOption(
            @NotBlank @Pattern(regexp = "[A-D]") String label,
            @NotBlank String text
    ) {}
}