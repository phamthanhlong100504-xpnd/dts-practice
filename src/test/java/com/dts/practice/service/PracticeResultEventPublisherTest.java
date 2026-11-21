package com.dts.practice.service;

import com.dts.practice.entity.Exam;
import com.dts.practice.entity.PresetExamSet;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.repository.ExamRepository;
import com.dts.practice.repository.PresetExamSetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticeResultEventPublisherTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private PresetExamSetRepository presetExamSetRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PracticeResultEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new PracticeResultEventPublisher(
                examRepository,
                presetExamSetRepository,
                kafkaTemplate,
                objectMapper,
                "learning-results"
        );
    }

    @Test
    void publishExamResult_RandomExam_SendsMessageToKafka() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        Exam exam = Exam.builder()
                .id(examId)
                .userId(userId)
                .examType("A1")
                .bankVersion("dts-2026-600-v1")
                .status(ExamStatus.COMPLETED)
                .score(88)
                .correctCount(22)
                .totalQuestions(25)
                .durationMinutes(19)
                .startedAt(now.minusSeconds(600))
                .completedAt(now)
                .build();

        when(examRepository.countByUserIdAndHistoryTargetIdAndCompletedAtIsNotNull(eq(userId), any(UUID.class)))
                .thenReturn(2);

        publisher.publishExamResult(exam, true);

        assertEquals(3, exam.getHistoryAttemptNo());
        assertNotNull(exam.getHistoryTargetId());
        verify(examRepository).save(exam);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals("learning-results", topicCaptor.getValue());
        assertEquals(userId.toString(), keyCaptor.getValue());

        String json = payloadCaptor.getValue();
        assertTrue(json.contains("\"eventType\":\"LEARNING_RESULT_CREATED\""));
        assertTrue(json.contains("\"activityType\":\"RANDOM_EXAM\""));
        assertTrue(json.contains("\"licenseClass\":\"A1\""));
        assertTrue(json.contains("\"result\":\"PASSED\""));
        assertTrue(json.contains("\"attemptNo\":3"));
    }

    @Test
    void publishExamResult_PresetExam_UsesPresetTitle() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID presetId = UUID.randomUUID();
        Instant now = Instant.now();

        Exam exam = Exam.builder()
                .id(examId)
                .userId(userId)
                .presetExamSetId(presetId)
                .examType("B")
                .bankVersion("dts-2026-600-v1")
                .status(ExamStatus.COMPLETED)
                .score(40)
                .correctCount(12)
                .totalQuestions(30)
                .durationMinutes(20)
                .startedAt(now.minusSeconds(500))
                .completedAt(now)
                .build();

        PresetExamSet preset = PresetExamSet.builder()
                .id(presetId)
                .title("Đề thi số 01 - B")
                .build();

        when(presetExamSetRepository.findById(presetId)).thenReturn(Optional.of(preset));
        when(examRepository.countByUserIdAndHistoryTargetIdAndCompletedAtIsNotNull(eq(userId), eq(presetId)))
                .thenReturn(0);

        publisher.publishExamResult(exam, false);

        assertEquals(presetId, exam.getHistoryTargetId());
        assertEquals(1, exam.getHistoryAttemptNo());

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("learning-results"), eq(userId.toString()), payloadCaptor.capture());

        String json = payloadCaptor.getValue();
        assertTrue(json.contains("\"displayTitle\":\"Đề thi số 01 - B\""));
        assertTrue(json.contains("\"activityType\":\"PRESET_EXAM\""));
        assertTrue(json.contains("\"result\":\"FAILED\""));
    }

    @Test
    void publishExamResult_KafkaError_CatchesAndDoesNotThrow() {
        UUID examId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Exam exam = Exam.builder()
                .id(examId)
                .userId(userId)
                .examType("A1")
                .status(ExamStatus.COMPLETED)
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka timeout"));

        assertDoesNotThrow(() -> publisher.publishExamResult(exam, true));
    }
}
