package com.dts.practice.service;

import com.dts.practice.dto.response.CriticalSessionResponse;
import com.dts.practice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriticalCursorService {

    private static final String CURRENT_BANK = CurrentQuestionBank.currentVersion();

    private final JdbcTemplate jdbcTemplate;
    private final CriticalSessionService criticalSessionService;

    @Transactional
    public CriticalSessionResponse saveCursor(
            UUID sessionId, UUID userId, Integer questionId
    ) {
        List<Session> sessions = jdbcTemplate.query("""
                SELECT license_class, bank_version, status, current_question_id
                FROM critical_question_sessions
                WHERE id = ? AND user_id = ?
                FOR UPDATE
                """, (rs, rowNum) -> new Session(
                rs.getString("license_class"), rs.getString("bank_version"),
                rs.getString("status"),
                rs.getObject("current_question_id", Integer.class)),
                sessionId, userId);
        if (sessions.size() != 1) {
            throw BusinessException.notFound("Critical-question session not found");
        }
        Session session = sessions.get(0);
        if (!CURRENT_BANK.equals(session.bankVersion())) {
            throw BusinessException.conflict("Question bank version changed");
        }
        if (!"IN_PROGRESS".equals(session.status())) {
            throw BusinessException.conflict("Critical-question session is completed");
        }

        List<Integer> positions = jdbcTemplate.query("""
                SELECT sq.question_order
                FROM critical_question_session_questions sq
                JOIN questions q ON q.id = sq.question_id
                WHERE sq.session_id = ? AND sq.question_id = ?
                  AND q.bank_version = ? AND q.is_critical = TRUE
                  AND jsonb_exists(q.critical_licenses, ?)
                  AND jsonb_exists(q.applicable_licenses, ?)
                """, (rs, rowNum) -> rs.getInt("question_order"),
                sessionId, questionId, CURRENT_BANK,
                session.licenseClass(), session.licenseClass());
        if (positions.size() != 1) {
            throw BusinessException.badRequest(
                    "Question is not in this critical-question round");
        }

        if (!questionId.equals(session.currentQuestionId())) {
            jdbcTemplate.update("""
                    UPDATE critical_question_sessions
                    SET current_question_id = ?, updated_at = NOW()
                    WHERE id = ? AND user_id = ? AND status = 'IN_PROGRESS'
                    """, questionId, sessionId, userId);
        }

        return criticalSessionService.get(sessionId, userId);
    }

    private record Session(String licenseClass, String bankVersion,
                           String status, Integer currentQuestionId) {}
}