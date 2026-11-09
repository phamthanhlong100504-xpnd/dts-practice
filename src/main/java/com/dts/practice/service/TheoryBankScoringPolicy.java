package com.dts.practice.service;

/**
 * Exam scoring rules must be pinned to the session's immutable bank version,
 * not to the bank selected for new sessions. A newly staged bank must NOT be
 * added to this allowlist without independent examination-policy verification.
 * This class deliberately does not activate or select any bank.
 */
public final class TheoryBankScoringPolicy {
    private TheoryBankScoringPolicy() {}

    public static boolean usesModernExamMatrix(String sessionBankVersion) {
        return "dts-2026-600-v1".equals(sessionBankVersion);
    }
}
