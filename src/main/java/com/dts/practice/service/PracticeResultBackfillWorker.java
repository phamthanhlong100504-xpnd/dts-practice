package com.dts.practice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name="exam.result-backfill.enabled",havingValue="true",matchIfMissing=true)
public class PracticeResultBackfillWorker {
    private final JdbcTemplate jdbc;
    private final ExamService exams;
    private final TransactionTemplate transaction;
    public PracticeResultBackfillWorker(JdbcTemplate jdbc,ExamService exams,PlatformTransactionManager manager) {
        this.jdbc=jdbc;this.exams=exams;this.transaction=new TransactionTemplate(manager);
    }
    @Scheduled(scheduler="practiceHistoryScheduler",fixedDelayString="${exam.result-backfill.poll-ms:15000}")
    public void backfill() {
        // Outbox uniqueness is the per-source checkpoint; failed rows cannot starve later sources.
        var batch=jdbc.query("""
            SELECT e.id,e.user_id,EXISTS(SELECT 1 FROM exam_progress_receipts r
                WHERE r.exam_id=e.id AND r.user_id=e.user_id) AS has_receipt
            FROM exams e WHERE e.mode='EXAM' AND e.status IN ('COMPLETED','TIMEOUT')
              AND e.completed_at IS NOT NULL
              AND NOT EXISTS(SELECT 1 FROM practice_result_outbox o WHERE o.exam_id=e.id)
              AND NOT EXISTS(SELECT 1 FROM practice_result_backfill_failures f WHERE f.exam_id=e.id)
            ORDER BY e.completed_at,e.id LIMIT 20
            """,(rs,row)->new Source(rs.getObject("id",UUID.class),rs.getObject("user_id",UUID.class),rs.getBoolean("has_receipt")));
        for(var source:batch) {
            try {
                transaction.executeWithoutResult(status -> {
                    Boolean passed=source.receipt() ? null : exams.getExamResult(source.id(),source.user()).passed();
                    // Finished sources only: getExamResult does not create receipts or Progress events here.
                    jdbc.queryForObject("SELECT dts_enqueue_practice_result(?,?)",Boolean.class,source.id(),passed);
                });
            } catch(RuntimeException failure) {
                jdbc.update("""
                    INSERT INTO practice_result_backfill_failures(exam_id,error_code) VALUES(?,?)
                    ON CONFLICT(exam_id) DO UPDATE SET error_code=EXCLUDED.error_code,failed_at=NOW()
                    """,source.id(),failure.getClass().getSimpleName());
                log.warn("Practice result backfill needs reconciliation: exam={}, error={}",source.id(),failure.getClass().getSimpleName());
            }
        }
    }
    private record Source(UUID id,UUID user,boolean receipt) {}
}
