package com.dts.practice.service;

import com.dts.practice.exception.BusinessException;

import java.util.Map;

public final class ExamMatrix {
    private ExamMatrix() {}

    public record Rule(
            int totalQuestions,
            int durationMinutes,
            int requiredCorrect,
            Map<Integer, Integer> ordinaryByChapter
    ) {
        public Rule {
            int total = 1 + ordinaryByChapter.values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            if (total != totalQuestions) {
                throw new IllegalArgumentException(
                        "Invalid exam matrix: " + totalQuestions
                );
            }
        }
    }

    private static final Map<Integer, Integer> MOTORCYCLE = Map.of(
            1, 8,
            2, 1,
            3, 1,
            5, 8,
            6, 6
    );

    private static final Rule A1 = new Rule(25, 19, 21, MOTORCYCLE);
    private static final Rule A = new Rule(25, 19, 23, MOTORCYCLE);

    private static final Rule B = new Rule(
            30, 20, 27,
            Map.of(1, 8, 2, 1, 3, 1, 4, 1, 5, 10, 6, 8)
    );

    private static final Rule C1 = new Rule(
            35, 22, 32,
            Map.of(1, 10, 2, 1, 3, 2, 4, 1, 5, 11, 6, 9)
    );

    private static final Rule C = new Rule(
            40, 24, 36,
            Map.of(1, 11, 2, 1, 3, 2, 4, 1, 5, 13, 6, 11)
    );

    private static final Rule LARGE = new Rule(
            45, 26, 41,
            Map.of(1, 12, 2, 1, 3, 2, 4, 2, 5, 14, 6, 13)
    );

    private static final Map<String, Rule> BY_CLASS = Map.ofEntries(
            Map.entry("A1", A1),
            Map.entry("A", A),
            Map.entry("B1", A),
            Map.entry("B", B),
            Map.entry("C1", C1),
            Map.entry("C", C),
            Map.entry("D1", LARGE),
            Map.entry("D2", LARGE),
            Map.entry("D", LARGE),
            Map.entry("BE", LARGE),
            Map.entry("C1E", LARGE),
            Map.entry("CE", LARGE),
            Map.entry("D1E", LARGE),
            Map.entry("D2E", LARGE),
            Map.entry("DE", LARGE)
    );

    public static Rule forClass(String licenseClass) {
        Rule rule = BY_CLASS.get(licenseClass);

        if (rule == null) {
            throw BusinessException.badRequest(
                    "Hạng bằng không được hỗ trợ: " + licenseClass
            );
        }

        return rule;
    }
}