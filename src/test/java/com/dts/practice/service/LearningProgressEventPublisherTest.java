package com.dts.practice.service;

import com.dts.practice.dto.event.learning.ExamAttemptSubmittedKafkaEvent;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.entity.Question;
import com.dts.practice.repository.QuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningProgressEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private QuestionRepository questionRepository;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private LearningProgressEventPublisher publisher;

    private final String topic = "dts.practice.learning-progress.v1";

    @BeforeEach
    void setUp() {
        publisher = new LearningProgressEventPublisher(
                kafkaTemplate,
                questionRepository,
                objectMapper,
                topic,
                true
        );
    }

    @Test
    void testPublishTheoryExamAttempt() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();
        UUID cbQuestionId = UUID.randomUUID();

        Exam exam = Exam.builder()
                .id(examId)
                .userId(userId)
                .examType("B2")
                .bankVersion("dts-2026-600-v1")
                .completedAt(Instant.now())
                .build();

        ExamAnswer a1 = new ExamAnswer();
        a1.setQuestionId(1);
        a1.setSelectedAnswer("A");
        a1.setIsCorrect(true);
        a1.setAnsweredAt(Instant.now());

        ExamAnswer a2 = new ExamAnswer();
        a2.setQuestionId(25);
        a2.setSelectedAnswer(null);
        a2.setIsCorrect(false);

        Question q1 = new Question();
        q1.setId(1);
        q1.setContentBuilderQuestionId(cbQuestionId);

        when(questionRepository.findAllById(List.of(1, 25))).thenReturn(List.of(q1));

        publisher.publishTheoryExamAttempt(exam, List.of(a1, a2));

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(1)).send(eq(topic), eq(userId.toString()), payloadCaptor.capture());

        String json = payloadCaptor.getValue();
        ExamAttemptSubmittedKafkaEvent event = objectMapper.readValue(json, ExamAttemptSubmittedKafkaEvent.class);

        assertEquals("EXAM_ATTEMPT_SUBMITTED", event.getEventType());
        assertEquals(1, event.getSchemaVersion());
        assertEquals("dts-practice", event.getSourceService());
        assertEquals("THEORY_EXAM", event.getActivityType());
        assertEquals(userId, event.getUserId());
        assertEquals("B2", event.getLicenseClass());
        assertEquals(examId, event.getSessionId());
        assertEquals(2, event.getItems().size());

        ExamAttemptSubmittedKafkaEvent.Item item1 = event.getItems().get(0);
        assertTrue(item1.getSourceOutcomeKey().startsWith("exam-"));
        assertEquals(1, item1.getPracticeQuestionId());
        assertEquals(cbQuestionId, item1.getContentBuilderQuestionId());
        assertEquals("CORRECT", item1.getOutcome());
        assertNull(item1.getScore());

        ExamAttemptSubmittedKafkaEvent.Item item2 = event.getItems().get(1);
        assertEquals(25, item2.getPracticeQuestionId());
        assertEquals("UNANSWERED", item2.getOutcome());
    }

    @Test
    void testPublishSimulationExam() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        ExamAttemptSubmittedKafkaEvent.Item item = ExamAttemptSubmittedKafkaEvent.Item.builder()
                .sourceOutcomeKey("sim-exam-b8a91c2d-scenario-12")
                .scenarioNumber(12)
                .scenarioId(null)
                .score(5)
                .firstResponseMs(2850)
                .durationMs(29000)
                .outcome("CORRECT")
                .answeredAt(Instant.now())
                .build();

        publisher.publishSimulationExam(sessionId, userId, "B2", "dts-2026-600-v1", Instant.now(), List.of(item));

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(1)).send(eq(topic), eq(userId.toString()), payloadCaptor.capture());

        ExamAttemptSubmittedKafkaEvent event = objectMapper.readValue(payloadCaptor.getValue(), ExamAttemptSubmittedKafkaEvent.class);
        assertEquals("SIMULATION", event.getActivityType());
        assertEquals("dts-simulation-service", event.getSourceService());
        assertEquals(1, event.getItems().size());
        assertEquals(12, event.getItems().get(0).getScenarioNumber());
        assertEquals(5, event.getItems().get(0).getScore());
        assertEquals(2850, event.getItems().get(0).getFirstResponseMs());
        assertEquals("CORRECT", event.getItems().get(0).getOutcome());
    }

    @Test
    void testPublishSimulationPractice() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();

        publisher.publishSimulationPractice(attemptId, userId, "B2", "dts-2026-600-v1", 15, 4, 3100, 30000, Instant.now());

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(1)).send(eq(topic), eq(userId.toString()), payloadCaptor.capture());

        ExamAttemptSubmittedKafkaEvent event = objectMapper.readValue(payloadCaptor.getValue(), ExamAttemptSubmittedKafkaEvent.class);
        assertEquals("SIMULATION", event.getActivityType());
        assertEquals("dts-simulation-service", event.getSourceService());
        assertEquals(1, event.getItems().size());
        assertEquals(15, event.getItems().get(0).getScenarioNumber());
        assertEquals(4, event.getItems().get(0).getScore());
        assertEquals(3100, event.getItems().get(0).getFirstResponseMs());
        assertEquals("CORRECT", event.getItems().get(0).getOutcome());
    }
}
