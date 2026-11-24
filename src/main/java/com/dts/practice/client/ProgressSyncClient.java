package com.dts.practice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class ProgressSyncClient {

    private final RestClient progressClient;
    private final String internalKey;

    public ProgressSyncClient(
            RestClient.Builder builder,
            @Value("${exam.progress-outbox.progress-url:${PROGRESS_SERVICE_URL:http://dts-progress:8083}}")
            String progressUrl,
            @Value("${exam.progress-outbox.internal-key:${EXAM_PROGRESS_INTERNAL_KEY:dts-internal-secret-progress-2024}}")
            String internalKey
    ) {
        this.internalKey = internalKey;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);

        this.progressClient = builder != null
                ? builder.baseUrl(progressUrl != null ? progressUrl : "http://dts-progress:8083")
                         .requestFactory(factory)
                         .build()
                : null;
    }

    public List<WrongQuestionSyncDto> getWrongQuestions(UUID userId, String licenseClass) {
        if (progressClient == null) return List.of();
        try {
            ApiResponseDto<List<WrongQuestionSyncDto>> response = progressClient.get()
                    .uri("/internal/v1/practice/users/{userId}/wrong-questions?licenseClass={license}", userId, licenseClass)
                    .header("X-DTS-Internal-Key", internalKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponseDto<List<WrongQuestionSyncDto>>>() {});
            return response != null && response.data() != null ? response.data() : List.of();
        } catch (Exception ex) {
            log.warn("Could not fetch wrong questions from dts-progress for user {}: {}", userId, ex.getMessage());
            return List.of();
        }
    }

    public CriticalOverviewSyncDto getCriticalOverview(UUID userId, String licenseClass) {
        if (progressClient == null) return null;
        try {
            ApiResponseDto<CriticalOverviewSyncDto> response = progressClient.get()
                    .uri("/internal/v1/practice/users/{userId}/critical-overview?licenseClass={license}", userId, licenseClass)
                    .header("X-DTS-Internal-Key", internalKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponseDto<CriticalOverviewSyncDto>>() {});
            return response != null ? response.data() : null;
        } catch (Exception ex) {
            log.warn("Could not fetch critical overview from dts-progress for user {}: {}", userId, ex.getMessage());
            return null;
        }
    }

    public Set<Integer> getBookmarks(UUID userId, String licenseClass) {
        if (progressClient == null) return Set.of();
        try {
            ApiResponseDto<List<Integer>> response = progressClient.get()
                    .uri("/internal/v1/practice/users/{userId}/bookmarks?licenseClass={license}", userId, licenseClass)
                    .header("X-DTS-Internal-Key", internalKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponseDto<List<Integer>>>() {});
            return response != null && response.data() != null ? new HashSet<>(response.data()) : Set.of();
        } catch (Exception ex) {
            log.warn("Could not fetch bookmarks from dts-progress for user {}: {}", userId, ex.getMessage());
            return Set.of();
        }
    }

    public void syncQuestionProgress(
            UUID userId, String licenseClass, String bankVersion,
            Integer questionId, Integer chapterId, Boolean critical,
            String dimension, Boolean learned, Boolean needsReview, Instant studiedAt
    ) {
        if (progressClient == null) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", UUID.randomUUID());
        payload.put("eventType", "QUESTION_PROGRESS_CHANGED");
        payload.put("schemaVersion", 1);
        payload.put("source", "dts-practice");
        payload.put("eventSequence", System.currentTimeMillis());
        payload.put("userId", userId);
        payload.put("licenseClass", licenseClass);
        payload.put("bankVersion", bankVersion);
        payload.put("questionId", questionId);
        payload.put("chapterId", chapterId != null ? chapterId : 1);
        payload.put("critical", Boolean.TRUE.equals(critical));
        payload.put("dimension", dimension);
        if (learned != null) payload.put("learned", learned);
        if (needsReview != null) payload.put("needsReview", needsReview);
        payload.put("studiedAt", studiedAt != null ? studiedAt : Instant.now());

        try {
            progressClient.post()
                    .uri("/internal/v1/practice/question-progress")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-DTS-Internal-Key", internalKey)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Synced question progress to dts-progress: user {}, question {}, dimension {}", userId, questionId, dimension);
        } catch (Exception ex) {
            log.warn("Non-fatal: could not sync question progress to dts-progress: {}", ex.getMessage());
        }
    }
}
