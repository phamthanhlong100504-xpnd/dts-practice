CREATE UNIQUE INDEX uq_exams_one_active_per_user_license
    ON exams (user_id, exam_type)
    WHERE status = 'IN_PROGRESS';