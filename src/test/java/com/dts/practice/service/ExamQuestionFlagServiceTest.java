package com.dts.practice.service;

import com.dts.practice.dto.response.ExamFlagsResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamQuestionFlagServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamAnswerRepository examAnswerRepository;

    @InjectMocks
    private ExamQuestionFlagService flagService;

    private UUID examId;
    private UUID ownerId;
    private Exam exam;

    @BeforeEach
    void setUp() {
        examId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        exam = Exam.builder()
                .id(examId)
                .userId(ownerId)
                .questionIds(List.of(102, 100, 101))
                .status(ExamStatus.IN_PROGRESS)
                .build();
    }

    @Test
    void getFlagsReturnsExamQuestionOrder() {
        ExamAnswer first = answer(101, true);
        ExamAnswer second = answer(102, true);

        when(examRepository.findByIdAndUserId(examId, ownerId))
                .thenReturn(Optional.of(exam));
        when(examAnswerRepository.findByExamIdOrderByQuestionId(examId))
                .thenReturn(List.of(first, second));

        ExamFlagsResponse response = flagService.getFlags(examId, ownerId);

        assertEquals(examId, response.examId());
        assertEquals(List.of(102, 101), response.flaggedQuestionIds());
        verify(examAnswerRepository, never()).save(any());
    }

    @Test
    void flagsUnansweredQuestionWithoutSubmittingAnswer() {
        ExamAnswer answer = answer(100, false);

        when(examRepository.findByIdAndUserIdForUpdate(examId, ownerId))
                .thenReturn(Optional.of(exam));
        when(examAnswerRepository.findByExamIdOrderByQuestionId(examId))
                .thenReturn(List.of(answer));

        ExamFlagsResponse response = flagService.updateFlag(
                examId, ownerId, 100, true
        );

        assertTrue(answer.isFlagged());
        assertNull(answer.getSelectedAnswer());
        assertEquals(List.of(100), response.flaggedQuestionIds());
        verify(examAnswerRepository).save(answer);
    }

    @Test
    void unflagsQuestion() {
        ExamAnswer answer = answer(100, true);

        when(examRepository.findByIdAndUserIdForUpdate(examId, ownerId))
                .thenReturn(Optional.of(exam));
        when(examAnswerRepository.findByExamIdOrderByQuestionId(examId))
                .thenReturn(List.of(answer));

        ExamFlagsResponse response = flagService.updateFlag(
                examId, ownerId, 100, false
        );

        assertFalse(answer.isFlagged());
        assertTrue(response.flaggedQuestionIds().isEmpty());
        verify(examAnswerRepository).save(answer);
    }

    @Test
    void settingSameFlagTwiceIsIdempotent() {
        ExamAnswer answer = answer(100, false);

        when(examRepository.findByIdAndUserIdForUpdate(examId, ownerId))
                .thenReturn(Optional.of(exam));
        when(examAnswerRepository.findByExamIdOrderByQuestionId(examId))
                .thenReturn(List.of(answer));

        ExamFlagsResponse first = flagService.updateFlag(
                examId, ownerId, 100, true
        );
        ExamFlagsResponse second = flagService.updateFlag(
                examId, ownerId, 100, true
        );

        assertEquals(List.of(100), first.flaggedQuestionIds());
        assertEquals(List.of(100), second.flaggedQuestionIds());
        verify(examAnswerRepository, times(2)).save(answer);
    }

    @Test
    void flagsRemainReadableAfterExamCompletes() {
        exam.setStatus(ExamStatus.COMPLETED);

        when(examRepository.findByIdAndUserId(examId, ownerId))
                .thenReturn(Optional.of(exam));
        when(examAnswerRepository.findByExamIdOrderByQuestionId(examId))
                .thenReturn(List.of(answer(100, true)));

        ExamFlagsResponse response = flagService.getFlags(examId, ownerId);

        assertEquals(List.of(100), response.flaggedQuestionIds());
    }

    @Test
    void anotherUserCannotReadFlags() {
        UUID otherUserId = UUID.randomUUID();

        when(examRepository.findByIdAndUserId(examId, otherUserId))
                .thenReturn(Optional.empty());

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> flagService.getFlags(examId, otherUserId)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        verifyNoInteractions(examAnswerRepository);
    }

    @Test
    void anotherUserCannotChangeFlags() {
        UUID otherUserId = UUID.randomUUID();

        when(examRepository.findByIdAndUserIdForUpdate(examId, otherUserId))
                .thenReturn(Optional.empty());

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> flagService.updateFlag(examId, otherUserId, 100, true)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        verifyNoInteractions(examAnswerRepository);
    }

    @Test
    void cannotFlagQuestionOutsideExam() {
        when(examRepository.findByIdAndUserIdForUpdate(examId, ownerId))
                .thenReturn(Optional.of(exam));

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> flagService.updateFlag(examId, ownerId, 999, true)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        verifyNoInteractions(examAnswerRepository);
    }

    private ExamAnswer answer(int questionId, boolean flagged) {
        return ExamAnswer.builder()
                .id(UUID.randomUUID())
                .exam(exam)
                .questionId(questionId)
                .flagged(flagged)
                .build();
    }
}