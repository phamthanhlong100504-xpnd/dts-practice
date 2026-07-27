package com.dts.practice.repository;

import com.dts.practice.entity.Exam;
import com.dts.practice.enums.ExamStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DisplayName("ExamRepository")
class ExamRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID userId;
    private Exam completedExam;
    private Exam inProgressExam;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        Instant now = Instant.now();

        completedExam = Exam.builder()
                .userId(userId).examType("A1")
                .questionIds(List.of(1, 2, 3)).totalQuestions(3)
                .status(ExamStatus.COMPLETED).score(90)
                .correctCount(2).wrongCount(1)
                .startedAt(now.minus(1, ChronoUnit.HOURS))
                .completedAt(now)
                .mode("EXAM").durationMinutes(20)
                .build();

        inProgressExam = Exam.builder()
                .userId(userId).examType("B1")
                .questionIds(List.of(4, 5, 6)).totalQuestions(3)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now)
                .mode("PRACTICE").durationMinutes(30)
                .expiresAt(now.plus(30, ChronoUnit.MINUTES))
                .build();

        entityManager.persist(completedExam);
        entityManager.persist(inProgressExam);
        entityManager.flush();
    }

    @Test
    @DisplayName("should find exam by id and userId")
    void shouldFindByIdAndUserId() {
        Optional<Exam> result = examRepository.findByIdAndUserId(completedExam.getId(), userId);

        assertThat(result).isPresent();
        assertThat(result.get().getExamType()).isEqualTo("A1");
    }

    @Test
    @DisplayName("should return empty when userId does not match")
    void shouldReturnEmptyForWrongUser() {
        Optional<Exam> result = examRepository.findByIdAndUserId(completedExam.getId(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should find exams by userId ordered by startedAt desc")
    void shouldFindByUserIdOrderedByDate() {
        Page<Exam> result = examRepository.findByUserIdOrderByStartedAtDesc(userId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getStartedAt())
                .isAfterOrEqualTo(result.getContent().get(1).getStartedAt());
    }

    @Test
    @DisplayName("should count exams by userId and status")
    void shouldCountByUserIdAndStatus() {
        long completed = examRepository.countByUserIdAndStatus(userId, ExamStatus.COMPLETED);
        long inProgress = examRepository.countByUserIdAndStatus(userId, ExamStatus.IN_PROGRESS);

        assertThat(completed).isEqualTo(1);
        assertThat(inProgress).isEqualTo(1);
    }

    @Test
    @DisplayName("should find expired in-progress exams")
    void shouldFindExpiredExams() {
        Instant future = Instant.now().plus(1, ChronoUnit.HOURS);
        List<Exam> expired = examRepository.findByStatusAndExpiresAtBefore(
                ExamStatus.IN_PROGRESS, future);

        assertThat(expired).hasSize(1);
    }
}
