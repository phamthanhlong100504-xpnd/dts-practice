package com.dts.practice.repository;

import com.dts.practice.config.JpaConfig;
import com.dts.practice.entity.Exam;
import com.dts.practice.enums.ExamStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(JpaConfig.class)
@DisplayName("ExamRepository")
class ExamRepositoryTest {

    private static final String BANK = "dts-2026-600-v1";

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

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
                .userId(userId)
                .examType("A1")
                .bankVersion(BANK)
                .questionIds(List.of(1, 2, 3))
                .totalQuestions(3)
                .status(ExamStatus.COMPLETED)
                .score(90)
                .correctCount(2)
                .wrongCount(1)
                .startedAt(now.minus(1, ChronoUnit.HOURS))
                .completedAt(now)
                .mode("EXAM")
                .durationMinutes(20)
                .build();

        inProgressExam = Exam.builder()
                .userId(userId)
                .examType("B1")
                .bankVersion(BANK)
                .questionIds(List.of(4, 5, 6))
                .totalQuestions(3)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now)
                .mode("PRACTICE")
                .durationMinutes(30)
                .expiresAt(now.plus(30, ChronoUnit.MINUTES))
                .build();

        entityManager.persist(completedExam);
        entityManager.persist(inProgressExam);
        entityManager.flush();
    }

    @Test
    @DisplayName("find exam by id and user")
    void shouldFindByIdAndUserId() {
        Optional<Exam> result = examRepository.findByIdAndUserId(
                completedExam.getId(), userId
        );

        assertThat(result).isPresent();
        assertThat(result.get().getExamType()).isEqualTo("A1");
    }

    @Test
    @DisplayName("do not return another user's exam")
    void shouldReturnEmptyForWrongUser() {
        Optional<Exam> result = examRepository.findByIdAndUserId(
                completedExam.getId(), UUID.randomUUID()
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("list user's exams by start time")
    void shouldFindByUserIdOrderedByDate() {
        Page<Exam> result =
                examRepository.findByUserIdOrderByStartedAtDesc(
                        userId, PageRequest.of(0, 10)
                );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getStartedAt())
                .isAfterOrEqualTo(
                        result.getContent().get(1).getStartedAt()
                );
    }

    @Test
    @DisplayName("count user's exams by status")
    void shouldCountByUserIdAndStatus() {
        long completed = examRepository.countByUserIdAndStatus(
                userId, ExamStatus.COMPLETED
        );
        long inProgress = examRepository.countByUserIdAndStatus(
                userId, ExamStatus.IN_PROGRESS
        );

        assertThat(completed).isEqualTo(1);
        assertThat(inProgress).isEqualTo(1);
    }

    @Test
    @DisplayName("find expired in-progress exams")
    void shouldFindExpiredExams() {
        Instant future = Instant.now().plus(1, ChronoUnit.HOURS);

        List<Exam> expired =
                examRepository.findByStatusAndExpiresAtBefore(
                        ExamStatus.IN_PROGRESS, future
                );

        assertThat(expired).hasSize(1);
    }

    @Test
    @DisplayName("active exam belongs to user, license, bank and valid time")
    void shouldFindOnlyApplicableActiveExam() {
        Instant now = Instant.now();

        Optional<Exam> active = examRepository
                .findFirstByUserIdAndExamTypeAndBankVersionAndStatusAndExpiresAtAfterOrderByStartedAtDesc(
                        userId, "B1", BANK, ExamStatus.IN_PROGRESS, now
                );

        assertThat(active).isPresent();
        assertThat(active.get().getId())
                .isEqualTo(inProgressExam.getId());

        assertThat(examRepository
                .findFirstByUserIdAndExamTypeAndBankVersionAndStatusAndExpiresAtAfterOrderByStartedAtDesc(
                        UUID.randomUUID(),
                        "B1",
                        BANK,
                        ExamStatus.IN_PROGRESS,
                        now
                )).isEmpty();

        assertThat(examRepository
                .findFirstByUserIdAndExamTypeAndBankVersionAndStatusAndExpiresAtAfterOrderByStartedAtDesc(
                        userId,
                        "A1",
                        BANK,
                        ExamStatus.IN_PROGRESS,
                        now
                )).isEmpty();

        assertThat(examRepository
                .findFirstByUserIdAndExamTypeAndBankVersionAndStatusAndExpiresAtAfterOrderByStartedAtDesc(
                        userId,
                        "B1",
                        "legacy-v1",
                        ExamStatus.IN_PROGRESS,
                        now
                )).isEmpty();

        assertThat(examRepository
                .findFirstByUserIdAndExamTypeAndBankVersionAndStatusAndExpiresAtAfterOrderByStartedAtDesc(
                        userId,
                        "B1",
                        BANK,
                        ExamStatus.IN_PROGRESS,
                        now.plus(31, ChronoUnit.MINUTES)
                )).isEmpty();
    }
}