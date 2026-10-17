package com.dts.practice.service;

import com.dts.practice.dto.response.ExamAnswerFeedbackResponse;
import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.entity.Question;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamSessionFeedbackService {

    private final ExamService examService;
    private final ExamAnswerRepository examAnswerRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public ExamSessionResponse getSession(UUID examId, UUID userId) {
        // ExamService xác minh chủ sở hữu và xử lý timeout trước khi trả session.
        ExamSessionResponse session =
                examService.getExamSession(examId, userId);

        if (!"PRACTICE".equals(session.mode())) {
            return session;
        }

        List<ExamAnswer> answered = examAnswerRepository
                .findByExamIdOrderByQuestionId(examId)
                .stream()
                .filter(answer -> answer.getSelectedAnswer() != null)
                .toList();

        if (answered.isEmpty()) {
            return session;
        }

        Map<Integer, Question> questionsById = questionRepository
                .findAllById(
                        answered.stream()
                                .map(ExamAnswer::getQuestionId)
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(
                        Question::getId,
                        question -> question
                ));

        Map<Integer, ExamAnswerFeedbackResponse> feedback =
                new LinkedHashMap<>();

        for (ExamAnswer answer : answered) {
            Question question = questionsById.get(answer.getQuestionId());

            if (question == null
                    || !session.answers().containsKey(
                            answer.getQuestionId()
                    )) {
                throw BusinessException.conflict(
                        "Exam answer is missing its question"
                );
            }

            Boolean correct = answer.getIsCorrect() != null
                    ? answer.getIsCorrect()
                    : Objects.equals(
                            question.getCorrectAnswer(),
                            answer.getSelectedAnswer()
                    );

            feedback.put(
                    answer.getQuestionId(),
                    new ExamAnswerFeedbackResponse(
                            answer.getSelectedAnswer(),
                            correct,
                            question.getCorrectAnswer(),
                            question.getExplanation() != null
                                    ? question.getExplanation()
                                    : ""
                    )
            );
        }

        return new ExamSessionResponse(
                session.examId(),
                session.examType(),
                session.status(),
                session.totalQuestions(),
                session.answeredCount(),
                session.durationMinutes(),
                session.expiresAt(),
                session.mode(),
                session.questions(),
                session.startedAt(),
                session.answers(),
                session.remainingTimeMs(),
                session.currentQuestionId(),
                feedback,
                session.serverTime()
        );
    }

    @Transactional
    public ExamSessionResponse pauseExam(
            UUID examId,
            UUID userId,
            Integer currentQuestionId
    ) {
        examService.pauseExam(examId, userId, currentQuestionId);
        return getSession(examId, userId);
    }

    @Transactional
    public ExamSessionResponse resumeExam(
            UUID examId,
            UUID userId
    ) {
        examService.resumeExam(examId, userId);
        return getSession(examId, userId);
    }
}