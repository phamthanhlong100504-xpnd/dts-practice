-- V6: Add username to exams for leaderboard display (denormalized from identity users at exam start)
ALTER TABLE exams ADD COLUMN IF NOT EXISTS username varchar(100);
