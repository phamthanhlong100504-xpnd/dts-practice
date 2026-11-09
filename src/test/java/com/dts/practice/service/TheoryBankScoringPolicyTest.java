package com.dts.practice.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TheoryBankScoringPolicyTest {
    @Test void v1RetainsItsOriginalScoringAfterBankSelectionChanges() {
        assertTrue(TheoryBankScoringPolicy.usesModernExamMatrix("dts-2026-600-v1"));
    }
    @Test void unknownOrFutureVersionsAreNotSilentlyCertified() {
        assertFalse(TheoryBankScoringPolicy.usesModernExamMatrix("dts-2026-600-v2"));
        assertFalse(TheoryBankScoringPolicy.usesModernExamMatrix("legacy-2025"));
        assertFalse(TheoryBankScoringPolicy.usesModernExamMatrix(""));
        assertFalse(TheoryBankScoringPolicy.usesModernExamMatrix(null));
    }
}
