# DTO reference — Practice

Tên trường và kiểu dưới đây lấy trực tiếp từ khai báo DTO hiện tại. Java `UUID`/`Instant`/`LocalDateTime` được serialize thành chuỗi; kiểu generic List/Map là mảng/object JSON. Nullability và trường bị ẩn phụ thuộc nhánh service, không chỉ kiểu Java. Các map có key số nguyên xuất hiện trong JSON dưới dạng key chuỗi.

## ActiveExamResponse

Ngoài các component dưới đây, `@JsonProperty("serverTime")` thêm trường `serverTime` kiểu Instant ở thời điểm trả response.

[`src/main/java/com/dts/practice/dto/response/ActiveExamResponse.java`](../../src/main/java/com/dts/practice/dto/response/ActiveExamResponse.java)

```java
public record ActiveExamResponse(
        UUID examId,
        String examType,
        String mode,
        String status,
        Integer totalQuestions,
        Integer answeredCount,
        Integer durationMinutes,
        Instant startedAt,
        Instant expiresAt,
        Long remainingTimeMs,
        Integer currentQuestionId
)
```

## ApiResponse

[`src/main/java/com/dts/practice/dto/response/ApiResponse.java`](../../src/main/java/com/dts/practice/dto/response/ApiResponse.java)

```java
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp,
        String traceId
)
```

## ChapterBookmarkResponse

[`src/main/java/com/dts/practice/dto/response/ChapterBookmarkResponse.java`](../../src/main/java/com/dts/practice/dto/response/ChapterBookmarkResponse.java)

```java
public record ChapterBookmarkResponse(
        int chapterId,
        Integer questionId,
        String licenseClass,
        boolean bookmarked
)
```

## ChapterBookmarksResponse

[`src/main/java/com/dts/practice/dto/response/ChapterBookmarksResponse.java`](../../src/main/java/com/dts/practice/dto/response/ChapterBookmarksResponse.java)

```java
public record ChapterBookmarksResponse(
        int chapterId,
        String licenseClass,
        List<Integer> questionIds
)
```

## ChapterLearnedAnswerResponse

[`src/main/java/com/dts/practice/dto/response/ChapterLearnedAnswerResponse.java`](../../src/main/java/com/dts/practice/dto/response/ChapterLearnedAnswerResponse.java)

```java
public record ChapterLearnedAnswerResponse(
        Integer questionId,
        String selectedAnswer,
        boolean isCorrect,
        Instant learnedAt,
        Instant lastAnsweredAt
)
```

## ChapterLearningAnswerResponse

[`src/main/java/com/dts/practice/dto/response/ChapterLearningAnswerResponse.java`](../../src/main/java/com/dts/practice/dto/response/ChapterLearningAnswerResponse.java)

```java
public record ChapterLearningAnswerResponse(
        Integer questionId,
        String selectedAnswer,
        boolean isCorrect,
        String correctAnswer,
        String explanation,
        Instant answeredAt
)
```

## ChapterLearningCursorResponse

[`src/main/java/com/dts/practice/dto/response/ChapterLearningCursorResponse.java`](../../src/main/java/com/dts/practice/dto/response/ChapterLearningCursorResponse.java)

```java
public record ChapterLearningCursorResponse(
        int chapterId,
        Integer lastQuestionId,
        Integer resumeQuestionId,
        Instant lastStudiedAt
)
```

## ChapterLearningOverviewResponse

[`src/main/java/com/dts/practice/dto/response/ChapterLearningOverviewResponse.java`](../../src/main/java/com/dts/practice/dto/response/ChapterLearningOverviewResponse.java)

```java
public record ChapterLearningOverviewResponse(
        String licenseClass,
        String bankVersion,
        int totalQuestions,
        int learnedQuestions,
        int completionPercent,
        List<Chapter> chapters
)

public record Chapter(
            int chapterId,
            int totalQuestions,
            int learnedQuestions,
            int completionPercent,
            String status,
            Integer lastQuestionId,
            Integer resumeQuestionId,
            Instant lastStudiedAt
    )
```

## ChapterReviewSessionResponse

[`src/main/java/com/dts/practice/dto/response/ChapterReviewSessionResponse.java`](../../src/main/java/com/dts/practice/dto/response/ChapterReviewSessionResponse.java)

```java
public record ChapterReviewSessionResponse(
        UUID reviewSessionId,
        String licenseClass,
        String bankVersion,
        int chapterId,
        String status,
        int totalQuestions,
        int answeredQuestions,
        int currentPosition,
        Integer currentQuestionId,
        List<Question> questions,
        Map<Integer, Answer> answers,
        Instant startedAt,
        Instant completedAt
)

public record Question(
            Integer questionId,
            int position,
            String questionText,
            Object options,
            boolean isCritical,
            String imageUrl,
            UUID mediaFileId,
            boolean bookmarked
    )

public record Answer(
            Integer questionId,
            String selectedAnswer,
            boolean isCorrect,
            String correctAnswer,
            String explanation,
            Instant answeredAt
    )
```

## CriticalAnswerResponse

[`src/main/java/com/dts/practice/dto/response/CriticalAnswerResponse.java`](../../src/main/java/com/dts/practice/dto/response/CriticalAnswerResponse.java)

```java
public record CriticalAnswerResponse(
        Integer questionId,
        String selectedAnswer,
        Boolean isCorrect,
        String correctAnswer,
        String explanation,
        Instant answeredAt
)
```

## CriticalProgressOverviewResponse

[`src/main/java/com/dts/practice/dto/response/CriticalProgressOverviewResponse.java`](../../src/main/java/com/dts/practice/dto/response/CriticalProgressOverviewResponse.java)

```java
public record CriticalProgressOverviewResponse(
        String licenseClass,
        int totalQuestions,
        int learnedQuestions,
        int completionPercent,
        int currentPosition,
        boolean hasProgress,
        boolean completed,
        Instant lastStudiedAt
)
```

## CriticalSessionResponse

[`src/main/java/com/dts/practice/dto/response/CriticalSessionResponse.java`](../../src/main/java/com/dts/practice/dto/response/CriticalSessionResponse.java)

```java
public record CriticalSessionResponse(
        UUID sessionId,
        String licenseClass,
        String bankVersion,
        String mode,
        String status,
        Integer totalQuestions,
        Integer answeredQuestions,
        Integer currentPosition,
        Integer currentQuestionId,
        List<Question> questions,
        Map<Integer, Answer> answers,
        Instant startedAt,
        Instant completedAt
)

public record Question(
            Integer questionId,
            Integer position,
            Integer chapterId,
            String questionText,
            Object options,
            Boolean isCritical,
            String imageUrl,
            UUID mediaFileId,
            Boolean bookmarked,
            Boolean learnedBeforeSession
    )

public record Answer(
            Integer questionId,
            String selectedAnswer,
            Boolean isCorrect,
            String correctAnswer,
            String explanation,
            Instant answeredAt
    )
```

## ErrorResponse

[`src/main/java/com/dts/practice/dto/response/ErrorResponse.java`](../../src/main/java/com/dts/practice/dto/response/ErrorResponse.java)

```java
public record ErrorResponse(
        String code,
        String message,
        List<String> details,
        String traceId,
        Instant timestamp
)
```

## ExamAnswerFeedbackResponse

[`src/main/java/com/dts/practice/dto/response/ExamAnswerFeedbackResponse.java`](../../src/main/java/com/dts/practice/dto/response/ExamAnswerFeedbackResponse.java)

```java
public record ExamAnswerFeedbackResponse(
        String selectedAnswer,
        Boolean isCorrect,
        String correctAnswer,
        String explanation
)
```

## ExamConfigResponse

[`src/main/java/com/dts/practice/dto/response/ExamConfigResponse.java`](../../src/main/java/com/dts/practice/dto/response/ExamConfigResponse.java)

```java
public record ExamConfigResponse(
        String examType,
        Integer totalQuestions,
        Integer durationMinutes,
        @Schema(description = "Minimum number of correct answers required to pass")
        Integer passingScore,
        Integer criticalQuestionCount
)
```

## ExamFlagsResponse

[`src/main/java/com/dts/practice/dto/response/ExamFlagsResponse.java`](../../src/main/java/com/dts/practice/dto/response/ExamFlagsResponse.java)

```java
public record ExamFlagsResponse(
        UUID examId,
        List<Integer> flaggedQuestionIds
)
```

## ExamHistoryResponse

[`src/main/java/com/dts/practice/dto/response/ExamHistoryResponse.java`](../../src/main/java/com/dts/practice/dto/response/ExamHistoryResponse.java)

```java
public record ExamHistoryResponse(
        UUID examId,
        String examType,
        String status,
        Integer totalQuestions,
        Integer correctCount,
        Integer wrongCount,
        Integer score,
        Boolean passed,
        String mode,
        Integer durationMinutes,
        Instant startedAt,
        Instant completedAt
)
```

## ExamProgressReceiptResponse

[`src/main/java/com/dts/practice/dto/response/ExamProgressReceiptResponse.java`](../../src/main/java/com/dts/practice/dto/response/ExamProgressReceiptResponse.java)

```java
public record ExamProgressReceiptResponse(
        UUID examId,
        String examType,
        String bankVersion,
        String mode,
        String status,
        Integer totalQuestions,
        Integer correctCount,
        Integer wrongCount,
        Integer answeredWrongCount,
        Integer unansweredCount,
        Integer score,
        Boolean passed,
        Integer durationSeconds,
        Instant startedAt,
        Instant completedAt
)
```

## ExamResultResponse

[`src/main/java/com/dts/practice/dto/response/ExamResultResponse.java`](../../src/main/java/com/dts/practice/dto/response/ExamResultResponse.java)

```java
public record ExamResultResponse(
        UUID examId,
        String examType,
        String status,
        Integer totalQuestions,
        Integer correctCount,
        Integer wrongCount,
        Integer score,
        Boolean passed,
        String mode,
        Integer durationMinutes,
        List<Map<String, Object>> answers,
        Instant startedAt,
        Instant completedAt,
        Integer answeredWrongCount,
        Integer unansweredCount
)
```

## ExamSessionResponse

[`src/main/java/com/dts/practice/dto/response/ExamSessionResponse.java`](../../src/main/java/com/dts/practice/dto/response/ExamSessionResponse.java)

```java
public record ExamSessionResponse(
        UUID examId,
        String examType,
        String status,
        Integer totalQuestions,
        Integer answeredCount,
        Integer durationMinutes,
        Instant expiresAt,
        String mode,
        List<QuestionResponse> questions,
        Instant startedAt,
        @Schema(description = "Selected options keyed by question ID")
        Map<Integer, String> answers,
        @Schema(description = "Server-calculated remaining time; frozen while PAUSED")
        Long remainingTimeMs,
        @Schema(description = "Saved question cursor within this exam")
        Integer currentQuestionId,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "Feedback for answered PRACTICE questions only; absent in EXAM mode")
        Map<Integer, ExamAnswerFeedbackResponse> answerFeedback,
        @Schema(description = "Server timestamp for synchronizing the countdown")
        Instant serverTime
)
```

## ImportMemoryTipBankRequest

[`src/main/java/com/dts/practice/dto/request/ImportMemoryTipBankRequest.java`](../../src/main/java/com/dts/practice/dto/request/ImportMemoryTipBankRequest.java)

```java
public record ImportMemoryTipBankRequest(

        @NotBlank
        @Pattern(regexp = "dts-2026-600-v1")
        String bankVersion,

        @NotEmpty
        @Size(min = 12, max = 12)
        List<@Valid BankMemoryTip> tips

)

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

    )
```

## ImportQuestionBankRequest

[`src/main/java/com/dts/practice/dto/request/ImportQuestionBankRequest.java`](../../src/main/java/com/dts/practice/dto/request/ImportQuestionBankRequest.java)

```java
public record ImportQuestionBankRequest(
        @NotBlank String bankVersion,
        @NotEmpty @Size(min = 600, max = 600)
        List<@Valid BankQuestion> questions
)

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
    )

public record BankOption(
            @NotBlank @Pattern(regexp = "[A-D]") String label,
            @NotBlank String text
    )
```

## ImportTrafficSignBankRequest

[`src/main/java/com/dts/practice/dto/request/ImportTrafficSignBankRequest.java`](../../src/main/java/com/dts/practice/dto/request/ImportTrafficSignBankRequest.java)

```java
public record ImportTrafficSignBankRequest(

        @NotBlank
        @Pattern(regexp = "dts-traffic-signs-v1")
        String bankVersion,

        @NotEmpty
        @Size(min = 221, max = 221)
        List<@Valid BankTrafficSign> signs

)

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

    )
```

## LearningBankCatalogResponse

[`src/main/java/com/dts/practice/dto/response/LearningBankCatalogResponse.java`](../../src/main/java/com/dts/practice/dto/response/LearningBankCatalogResponse.java)

```java
public record LearningBankCatalogResponse(
        String licenseClass,
        String bankVersion,
        int totalQuestions,
        int criticalQuestions,
        List<Chapter> chapters
)

public record Chapter(
            int chapterId,
            String chapterName,
            int totalQuestions,
            int criticalQuestions
    )
```

## LicenseCatalogItemResponse

[`src/main/java/com/dts/practice/dto/response/LicenseCatalogItemResponse.java`](../../src/main/java/com/dts/practice/dto/response/LicenseCatalogItemResponse.java)

```java
public record LicenseCatalogItemResponse(
        String code,
        String group,
        String title,
        String description,
        String bankVersion,
        @Schema(description = "Actual number of applicable questions in the current bank")
        long totalStudyQuestions,
        @Schema(description = "Actual number of critical questions for this license in the current bank")
        long criticalStudyQuestions,
        int totalQuestions,
        int durationMinutes,
        int passingScore,
        int criticalQuestionCount,
        @Schema(description = "Study counts match the curriculum and exam quotas can be filled")
        boolean bankCountsMatch,
        @Schema(description = "The current bank has enough eligible questions to generate an exam")
        boolean examAvailable
)
```

## MemoryTipListResponse

[`src/main/java/com/dts/practice/dto/response/MemoryTipListResponse.java`](../../src/main/java/com/dts/practice/dto/response/MemoryTipListResponse.java)

```java
public record MemoryTipListResponse(
        String bankVersion,
        List<MemoryTipResponse> items,
        long totalElements
)
```

## MemoryTipResponse

[`src/main/java/com/dts/practice/dto/response/MemoryTipResponse.java`](../../src/main/java/com/dts/practice/dto/response/MemoryTipResponse.java)

```java
public record MemoryTipResponse(

        @Schema(description = "Stable UUID from the existing memory_tips record")
        UUID id,

        Integer sourceTipId,
        Integer sortOrder,
        String title,
        String quickRule,
        String explanation
)
```

## PauseExamRequest

[`src/main/java/com/dts/practice/dto/request/PauseExamRequest.java`](../../src/main/java/com/dts/practice/dto/request/PauseExamRequest.java)

```java
public record PauseExamRequest(
        @NotNull Integer currentQuestionId
)
```

## PresetExamSetListResponse

[`src/main/java/com/dts/practice/dto/response/PresetExamSetListResponse.java`](../../src/main/java/com/dts/practice/dto/response/PresetExamSetListResponse.java)

```java
public record PresetExamSetListResponse(
        String licenseClass,
        int total,
        List<Item> items
)

public record Item(
            UUID id,
            String code,
            String title,
            String licenseClass,
            int totalQuestions,
            int durationSeconds,
            String status,
            int order,
            String userStatus,
            int attemptCount,
            LatestAttempt latestAttempt
    )

public record LatestAttempt(
            UUID sessionId,
            String status,
            Integer correctCount,
            Integer totalQuestions,
            Boolean passed,
            Instant startedAt,
            Instant completedAt
    )
```

## QuestionResponse

[`src/main/java/com/dts/practice/dto/response/QuestionResponse.java`](../../src/main/java/com/dts/practice/dto/response/QuestionResponse.java)

```java
public record QuestionResponse(
        Integer id,
        Integer chapter,
        String questionText,
        Object options,
        Boolean isCritical,
        String imageUrl,
        UUID mediaFileId,
        String correctAnswer,
        String explanation
)
```

## RecordChapterAnswerRequest

[`src/main/java/com/dts/practice/dto/request/RecordChapterAnswerRequest.java`](../../src/main/java/com/dts/practice/dto/request/RecordChapterAnswerRequest.java)

```java
public record RecordChapterAnswerRequest(
        @NotBlank String licenseClass,
        @NotNull @Positive Integer questionId,
        @NotBlank String selectedAnswer
)
```

## SaveChapterCursorRequest

[`src/main/java/com/dts/practice/dto/request/SaveChapterCursorRequest.java`](../../src/main/java/com/dts/practice/dto/request/SaveChapterCursorRequest.java)

```java
public record SaveChapterCursorRequest(
        @NotBlank String licenseClass,
        @NotNull @Positive Integer questionId
)
```

## SaveChapterReviewCursorRequest

[`src/main/java/com/dts/practice/dto/request/SaveChapterReviewCursorRequest.java`](../../src/main/java/com/dts/practice/dto/request/SaveChapterReviewCursorRequest.java)

```java
public record SaveChapterReviewCursorRequest(
        @NotNull Integer currentQuestionId
)
```

## SaveWrongReviewCursorRequest

[`src/main/java/com/dts/practice/dto/request/SaveWrongReviewCursorRequest.java`](../../src/main/java/com/dts/practice/dto/request/SaveWrongReviewCursorRequest.java)

```java
public record SaveWrongReviewCursorRequest(
        @NotNull Integer questionId
)
```

## SetChapterBookmarkRequest

[`src/main/java/com/dts/practice/dto/request/SetChapterBookmarkRequest.java`](../../src/main/java/com/dts/practice/dto/request/SetChapterBookmarkRequest.java)

```java
public record SetChapterBookmarkRequest(
        @NotBlank String licenseClass,
        @NotNull Boolean bookmarked
)
```

## StartChapterReviewRequest

[`src/main/java/com/dts/practice/dto/request/StartChapterReviewRequest.java`](../../src/main/java/com/dts/practice/dto/request/StartChapterReviewRequest.java)

```java
public record StartChapterReviewRequest(
        @NotBlank String licenseClass
)
```

## StartCriticalSessionRequest

[`src/main/java/com/dts/practice/dto/request/StartCriticalSessionRequest.java`](../../src/main/java/com/dts/practice/dto/request/StartCriticalSessionRequest.java)

```java
public record StartCriticalSessionRequest(
        @NotBlank String licenseClass,
        @NotBlank String mode,
        Boolean restart
)
```

## StartExamRequest

[`src/main/java/com/dts/practice/dto/request/StartExamRequest.java`](../../src/main/java/com/dts/practice/dto/request/StartExamRequest.java)

```java
public record StartExamRequest(
        @NotBlank String examType,
        @Min(1) @Max(60) Integer totalQuestions,
        @Min(5) @Max(120) Integer durationMinutes,
        String mode,
        // Full name of the user (from identity login) — denormalized for leaderboard display.
        // Client-provided; practice falls back to username when blank.
        String fullName
)
```

## StartPresetExamRequest

[`src/main/java/com/dts/practice/dto/request/StartPresetExamRequest.java`](../../src/main/java/com/dts/practice/dto/request/StartPresetExamRequest.java)

```java
public record StartPresetExamRequest(
        String licenseClass,
        String mode,
        String fullName
)
```

## StartWrongReviewRequest

[`src/main/java/com/dts/practice/dto/request/StartWrongReviewRequest.java`](../../src/main/java/com/dts/practice/dto/request/StartWrongReviewRequest.java)

```java
public record StartWrongReviewRequest(@NotBlank String licenseClass)
```

## SubmitAnswerRequest

[`src/main/java/com/dts/practice/dto/request/SubmitAnswerRequest.java`](../../src/main/java/com/dts/practice/dto/request/SubmitAnswerRequest.java)

```java
public record SubmitAnswerRequest(
        @NotBlank String questionId,
        @NotBlank String selectedAnswer
)
```

## SubmitAnswerResponse

[`src/main/java/com/dts/practice/dto/response/SubmitAnswerResponse.java`](../../src/main/java/com/dts/practice/dto/response/SubmitAnswerResponse.java)

```java
public record SubmitAnswerResponse(
        String status,
        Boolean isCorrect,
        String correctAnswer,
        String explanation
)
```

## SubmitChapterReviewAnswerRequest

[`src/main/java/com/dts/practice/dto/request/SubmitChapterReviewAnswerRequest.java`](../../src/main/java/com/dts/practice/dto/request/SubmitChapterReviewAnswerRequest.java)

```java
public record SubmitChapterReviewAnswerRequest(
        @NotNull Integer questionId,
        @NotBlank String selectedAnswer
)
```

## SubmitCriticalAnswerRequest

[`src/main/java/com/dts/practice/dto/request/SubmitCriticalAnswerRequest.java`](../../src/main/java/com/dts/practice/dto/request/SubmitCriticalAnswerRequest.java)

```java
public record SubmitCriticalAnswerRequest(
        @NotBlank String licenseClass,
        @NotNull UUID sessionId,
        @NotNull Integer questionId,
        @NotBlank String selectedAnswer
)
```

## SubmitWrongReviewAnswerRequest

[`src/main/java/com/dts/practice/dto/request/SubmitWrongReviewAnswerRequest.java`](../../src/main/java/com/dts/practice/dto/request/SubmitWrongReviewAnswerRequest.java)

```java
public record SubmitWrongReviewAnswerRequest(
        @NotNull Integer questionId,
        @NotBlank String selectedAnswer
)
```

## TrafficSignListResponse

[`src/main/java/com/dts/practice/dto/response/TrafficSignListResponse.java`](../../src/main/java/com/dts/practice/dto/response/TrafficSignListResponse.java)

```java
public record TrafficSignListResponse(
        String bankVersion,
        List<TrafficSignResponse> items,
        long totalElements,
        int page,
        int size,
        int totalPages,

        @Schema(description = "Counts for all five groups in the current published bank, before group and q filters")
        List<Group> groups
)

public record Group(
            String groupCode,
            String groupName,
            long totalElements
    )
```

## TrafficSignResponse

[`src/main/java/com/dts/practice/dto/response/TrafficSignResponse.java`](../../src/main/java/com/dts/practice/dto/response/TrafficSignResponse.java)

```java
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
)
```

## UpdateCriticalCursorRequest

[`src/main/java/com/dts/practice/dto/request/UpdateCriticalCursorRequest.java`](../../src/main/java/com/dts/practice/dto/request/UpdateCriticalCursorRequest.java)

```java
public record UpdateCriticalCursorRequest(
        @NotNull Integer questionId
)
```

## UpdateCriticalProgressRequest

[`src/main/java/com/dts/practice/dto/request/UpdateCriticalProgressRequest.java`](../../src/main/java/com/dts/practice/dto/request/UpdateCriticalProgressRequest.java)

```java
public record UpdateCriticalProgressRequest(
        @NotBlank String licenseClass,
        @NotNull Boolean learned
)
```

## UpdateExamCursorRequest

[`src/main/java/com/dts/practice/dto/request/UpdateExamCursorRequest.java`](../../src/main/java/com/dts/practice/dto/request/UpdateExamCursorRequest.java)

```java
public record UpdateExamCursorRequest(
        @NotNull Integer currentQuestionId
)
```

## UpdateExamFlagRequest

[`src/main/java/com/dts/practice/dto/request/UpdateExamFlagRequest.java`](../../src/main/java/com/dts/practice/dto/request/UpdateExamFlagRequest.java)

```java
public record UpdateExamFlagRequest(
        @NotNull Boolean flagged
)
```

## WrongQuestionOverviewResponse

[`src/main/java/com/dts/practice/dto/response/WrongQuestionOverviewResponse.java`](../../src/main/java/com/dts/practice/dto/response/WrongQuestionOverviewResponse.java)

```java
public record WrongQuestionOverviewResponse(
        String licenseClass,
        long totalWrongQuestions,
        boolean hasWrongQuestions
)
```

## WrongQuestionResponse

[`src/main/java/com/dts/practice/dto/response/WrongQuestionResponse.java`](../../src/main/java/com/dts/practice/dto/response/WrongQuestionResponse.java)

```java
public record WrongQuestionResponse(
        Integer questionId,
        Integer sourceQuestionId,
        Integer chapterId,
        String questionText,
        Object options,
        String selectedAnswer,
        String correctAnswer,
        Boolean isCritical,
        String explanation,
        String imageUrl,
        UUID mediaFileId,
        Instant lastWrongAt
)
```

## WrongReviewAnswerResponse

[`src/main/java/com/dts/practice/dto/response/WrongReviewAnswerResponse.java`](../../src/main/java/com/dts/practice/dto/response/WrongReviewAnswerResponse.java)

```java
public record WrongReviewAnswerResponse(
        Integer questionId,
        String selectedAnswer,
        Boolean isCorrect,
        String correctAnswer,
        String explanation,
        Instant answeredAt
)
```

## WrongReviewSessionResponse

[`src/main/java/com/dts/practice/dto/response/WrongReviewSessionResponse.java`](../../src/main/java/com/dts/practice/dto/response/WrongReviewSessionResponse.java)

```java
public record WrongReviewSessionResponse(
        UUID sessionId,
        String licenseClass,
        String bankVersion,
        String status,
        Integer totalQuestions,
        Integer currentPosition,
        Integer currentQuestionId,
        List<Question> questions,
        Map<Integer, Answer> answers,
        Integer answeredQuestions,
        Integer correctCount,
        Integer wrongCount,
        Integer unansweredCount,
        Long remainingCount,
        Instant startedAt,
        Instant completedAt
)

public record Question(
            Integer questionId,
            Integer position,
            Integer chapterId,
            String chapterTitle,
            String questionText,
            Object options,
            Boolean isCritical,
            String correctAnswer,
            String explanation,
            String imageUrl,
            UUID mediaFileId,
            String previousWrongAnswer,
            Instant lastWrongAt,
            Boolean bookmarked
    )

public record Answer(
            String selectedAnswer,
            Boolean isCorrect,
            Instant answeredAt
    )
```
