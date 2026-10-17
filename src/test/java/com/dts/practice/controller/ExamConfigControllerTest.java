package com.dts.practice.controller;

import com.dts.practice.dto.response.ExamConfigResponse;
import com.dts.practice.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExamConfigControllerTest {

    private final ExamConfigController controller = new ExamConfigController();

    @Test
    void returnsPublishedRulesForRepresentativeClasses() {
        assertConfig(" a1 ", "A1", 25, 19, 21, 1);
        assertConfig("B1", "B1", 25, 19, 23, 1);
        assertConfig("B", "B", 30, 20, 27, 1);
        assertConfig("C1", "C1", 35, 22, 32, 1);
        assertConfig("C", "C", 40, 24, 36, 1);
        assertConfig("D", "D", 45, 26, 41, 1);
    }

    @Test
    void rejectsUnknownOrEmptyLicense() {
        BusinessException unknown = assertThrows(
                BusinessException.class,
                () -> controller.getConfig("X")
        );
        assertEquals(HttpStatus.BAD_REQUEST, unknown.getStatus());

        BusinessException empty = assertThrows(
                BusinessException.class,
                () -> controller.getConfig(" ")
        );
        assertEquals(HttpStatus.BAD_REQUEST, empty.getStatus());
    }

    private void assertConfig(
            String requested,
            String expectedType,
            int questions,
            int minutes,
            int requiredCorrect,
            int critical
    ) {
        ExamConfigResponse data = controller.getConfig(requested).data();
        assertEquals(expectedType, data.examType());
        assertEquals(questions, data.totalQuestions());
        assertEquals(minutes, data.durationMinutes());
        assertEquals(requiredCorrect, data.passingScore());
        assertEquals(critical, data.criticalQuestionCount());
    }
}