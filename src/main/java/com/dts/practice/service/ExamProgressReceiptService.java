package com.dts.practice.service;

import com.dts.practice.dto.response.ExamProgressReceiptResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExamProgressReceiptService {

    private final JdbcTemplate jdbcTemplate;
    private final ExamRepository examRepository;
    private final ExamAnswerRepository examAnswerRepository;

    // Gọi trong cùng transaction kết thúc bài thi.
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(
            Exam exam,
            List<ExamAnswer> answers,
            Boolean passed
    ) {
        if (exam.getStatus() != ExamStatus.COMPLETED
                && exam.getStatus() != ExamStatus.TIMEOUT) {
            throw BusinessException.conflict("Exam is not finished");
        }

        if (exam.getCompletedAt() == null
                || exam.getStartedAt() == null
                || exam.getCorrectCount() == null
                || exam.getWrongCount() == null
                || exam.getTotalQuestions() == null
                || passed == null) {
            throw BusinessException.conflict(
                    "Exam progress is incomplete"
            );
        }

        int answeredWrong = (int) answers.stream()
                .filter(answer ->
                        answer.getSelectedAnswer() != null
                                && !answer.getSelectedAnswer().isBlank()
                                && !Boolean.TRUE.equals(
                                        answer.getIsCorrect()
                                )
                )
                .count();

        int unanswered = exam.getTotalQuestions()
                - exam.getCorrectCount()
                - answeredWrong;

        if (unanswered < 0) {
            throw BusinessException.conflict(
                    "Exam progress counts do not match"
            );
        }

        jdbcTemplate.update("""
                INSERT INTO exam_progress_receipts (
                    user_id,
                    exam_id,
                    exam_type,
                    bank_version,
                    mode,
                    status,
                    total_questions,
                    correct_count,
                    wrong_count,
                    answered_wrong_count,
                    unanswered_count,
                    score,
                    passed,
                    duration_seconds,
                    started_at,
                    completed_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (user_id, exam_id) DO NOTHING
                """,
                exam.getUserId(),
                exam.getId(),
                exam.getExamType(),
                exam.getBankVersion(),
                exam.getMode(),
                exam.getStatus().name(),
                exam.getTotalQuestions(),
                exam.getCorrectCount(),
                exam.getWrongCount(),
                answeredWrong,
                unanswered,
                score(exam),
                passed,
                durationSeconds(exam),
                Timestamp.from(exam.getStartedAt()),
                Timestamp.from(exam.getCompletedAt())
        );

        // Một examId chỉ tạo một logical EXAM_FINISHED event.
        // Receipt và outbox cùng commit với trạng thái kết thúc bài.
        jdbcTemplate.update("""
                INSERT INTO exam_progress_outbox (
                    event_id,
                    user_id,
                    exam_id,
                    event_type,
                    schema_version,
                    payload
                )
                SELECT
                    r.exam_id,
                    r.user_id,
                    r.exam_id,
                    'EXAM_FINISHED',
                    1,
                    jsonb_build_object(
                        'eventId', r.exam_id,
                        'eventType', 'EXAM_FINISHED',
                        'schemaVersion', 1,
                        'source', 'dts-practice',
                        'userId', r.user_id,
                        'examId', r.exam_id,
                        'licenseClass', r.exam_type,
                        'bankVersion', r.bank_version,
                        'mode', r.mode,
                        'status', r.status,
                        'totalQuestions', r.total_questions,
                        'correctCount', r.correct_count,
                        'wrongCountLegacy', r.wrong_count,
                        'answeredWrongCount', r.answered_wrong_count,
                        'unansweredCount', r.unanswered_count,
                        'score', r.score,
                        'passed', r.passed,
                        'durationSeconds', r.duration_seconds,
                        'startedAt', r.started_at,
                        'completedAt', r.completed_at
                    )
                FROM exam_progress_receipts r
                WHERE r.user_id = ?
                  AND r.exam_id = ?
                ON CONFLICT (event_id) DO NOTHING
                """,
                exam.getUserId(),
                exam.getId()
        );
    }

    // Cho phép lấy receipt của bài đã hoàn thành trước migration.
    @Transactional
    public ExamProgressReceiptResponse getReceipt(
            UUID examId,
            UUID userId,
            Boolean passed
    ) {
        Exam exam = examRepository
                .findByIdAndUserIdForUpdate(examId, userId)
                .orElseThrow(
                        () -> BusinessException.notFound(
                                "Exam not found"
                        )
                );

        if (exam.getStatus() != ExamStatus.COMPLETED
                && exam.getStatus() != ExamStatus.TIMEOUT) {
            throw BusinessException.conflict(
                    "Exam is not finished"
            );
        }

        record(
                exam,
                examAnswerRepository
                        .findByExamIdOrderByQuestionId(examId),
                passed
        );

        return jdbcTemplate.queryForObject("""
                SELECT
                    exam_id,
                    exam_type,
                    bank_version,
                    mode,
                    status,
                    total_questions,
                    correct_count,
                    wrong_count,
                    answered_wrong_count,
                    unanswered_count,
                    score,
                    passed,
                    duration_seconds,
                    started_at,
                    completed_at
                FROM exam_progress_receipts
                WHERE user_id = ?
                  AND exam_id = ?
                """,
                (rs, row) -> new ExamProgressReceiptResponse(
                        rs.getObject("exam_id", UUID.class),
                        rs.getString("exam_type"),
                        rs.getString("bank_version"),
                        rs.getString("mode"),
                        rs.getString("status"),
                        rs.getInt("total_questions"),
                        rs.getInt("correct_count"),
                        rs.getInt("wrong_count"),
                        rs.getInt("answered_wrong_count"),
                        rs.getInt("unanswered_count"),
                        rs.getInt("score"),
                        rs.getObject("passed", Boolean.class),
                        rs.getInt("duration_seconds"),
                        rs.getTimestamp("started_at").toInstant(),
                        rs.getTimestamp("completed_at").toInstant()
                ),
                userId,
                examId
        );
    }

    private int durationSeconds(Exam exam) {
        long allowedMs = exam.getDurationMinutes() == null
                ? Long.MAX_VALUE
                : Duration.ofMinutes(
                        exam.getDurationMinutes()
                ).toMillis();

        long usedMs;

        if (exam.getExpiresAt() != null
                && exam.getDurationMinutes() != null) {
            long remainingMs = Math.max(
                    0L,
                    Duration.between(
                            exam.getCompletedAt(),
                            exam.getExpiresAt()
                    ).toMillis()
            );

            usedMs = Math.max(
                    0L,
                    allowedMs - remainingMs
            );
        } else {
            usedMs = Math.max(
                    0L,
                    Duration.between(
                            exam.getStartedAt(),
                            exam.getCompletedAt()
                    ).toMillis()
            );
        }

        long seconds = Math.min(
                allowedMs,
                usedMs
        ) / 1000L;

        return (int) Math.min(
                Integer.MAX_VALUE,
                seconds
        );
    }

    private int score(Exam exam) {
        if (exam.getScore() != null) {
            return exam.getScore();
        }

        return exam.getTotalQuestions() == 0
                ? 0
                : (int) Math.round(
                        (double) exam.getCorrectCount()
                                / exam.getTotalQuestions()
                                * 100
                );
    }
}