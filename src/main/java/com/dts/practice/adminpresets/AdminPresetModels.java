package com.dts.practice.adminpresets;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Public administrator contracts; sourceQuestionIds are NOT Practice database question IDs. */
public final class AdminPresetModels {
    private AdminPresetModels() {}

    public record Create(String code, String title, String licenseClass, Integer displayOrder) {}
    public record Edit(long expectedRevision, String code, String title, Integer displayOrder) {}
    public record Questions(long expectedRevision, List<Integer> sourceQuestionIds) {}
    public record Summary(UUID id, UUID logicalSetId, UUID sourcePublishedSetId,
                          String code, String title, String licenseClass, String bankVersion,
                          int versionNumber, int totalQuestions, int durationMinutes,
                          int displayOrder, String status, long revision,
                          String validationStatus, Instant updatedAt, boolean canPublish) {}
    public record Detail(Summary metadata, List<Integer> sourceQuestionIds) {}
    public record Page(long total, List<Summary> items, int limit, int offset) {}
    public record SelectableQuestion(int sourceQuestionId, int chapter, String questionText,
                                     boolean criticalForLicense, boolean hasMedia) {}
    public record QuestionPage(long total, List<SelectableQuestion> items, int limit, int offset) {}
    public record Validation(UUID draftId, long revision, boolean valid,
                             int totalQuestions, int criticalQuestionCount,
                             String reasonCode, boolean canPublish) {}
    public record Matrix(String licenseClass, String bankVersion,
                         String matrixPolicyVersion, int totalQuestions,
                         int durationMinutes, int requiredCorrect,
                         int criticalQuestionCount, Map<Integer, Integer> chapterQuotas,
                         Instant effectiveFrom, Instant effectiveUntil,
                         boolean policyVersioned, boolean editable) {}
}
