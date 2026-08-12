-- V7: Add full_name to exams for leaderboard display (denormalized from FE at exam start, fallback username)
ALTER TABLE exams ADD COLUMN IF NOT EXISTS full_name varchar(100);
