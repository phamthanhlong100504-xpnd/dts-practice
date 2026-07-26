package com.dts.practice.service;

import com.dts.practice.dto.request.StartExamRequest;
import com.dts.practice.dto.request.SubmitAnswerRequest;
import com.dts.practice.dto.response.ExamResultResponse;
import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.entity.Question;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import com.dts.practice.repository.QuestionRepository;
import com.dts.practice.security.JwtUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExamService {

    private static final int DEFAULT_TOTAL_QUESTIONS = 25;
    private static final int REQUIRED_CORRECT_FOR_PASS = 21;
    private static final int CRITICAL_QUESTIONS_IN_EXAM = 1;

    private final ExamRepository examRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public ExamSessionResponse startExam(JwtUserDetails user, StartExamRequest request) {
        int totalQuestions = request.totalQuestions() != null ? request.totalQuestions() : DEFAULT_TOTAL_QUESTIONS;

        List<Question> criticalQuestions = questionRepository.findRandomCriticalQuestions(CRITICAL_QUESTIONS_IN_EXAM);
        List<Integer> selectedIds = new ArrayList<>(criticalQuestions.stream().map(Question::getId).toList());

        int remaining = totalQuestions - selectedIds.size();
        List<Question> normalQuestions = questionRepository.findRandomNonCritical(remaining);
        selectedIds.addAll(normalQuestions.stream().map(Question::getId).toList());

        Collections.shuffle(selectedIds);

        Exam exam = Exam.builder()
                .userId(user.userId())
                .examType(request.examType())
                .questionIds(selectedIds)
                .totalQuestions(totalQuestions)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(Instant.now())
                .build();
        exam = examRepository.save(exam);

        for (Integer qId : selectedIds) {
            examAnswerRepository.save(ExamAnswer.builder()
                    .exam(exam)
                    .questionId(qId)
                    .build());
        }

        List<Question> questions = questionRepository.findAllById(selectedIds);
        Map<Integer, Question> qMap = new HashMap<>();
        for (Question q : questions) qMap.put(q.getId(), q);

        List<QuestionResponse> questionResponses = new ArrayList<>();
        for (Integer qId : selectedIds) {
            Question q = qMap.get(qId);
            if (q != null) {
                questionResponses.add(toQuestionResponse(q));
            }
        }

        return new ExamSessionResponse(
                exam.getId(),
                exam.getExamType(),
                exam.getStatus().name(),
                exam.getTotalQuestions(),
                0,
                questionResponses,
                exam.getStartedAt()
        );
    }

    public ExamSessionResponse getExamSession(UUID examId, UUID userId) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        List<ExamAnswer> answers = examAnswerRepository.findByExamIdOrderByQuestionId(examId);
        long answeredCount = answers.stream().filter(a -> a.getSelectedAnswer() != null).count();

        List<Question> questions = questionRepository.findAllById(exam.getQuestionIds());
        Map<Integer, Question> qMap = new HashMap<>();
        for (Question q : questions) qMap.put(q.getId(), q);

        List<QuestionResponse> questionResponses = new ArrayList<>();
        for (Integer qId : exam.getQuestionIds()) {
            Question q = qMap.get(qId);
            if (q != null) {
                questionResponses.add(toQuestionResponse(q));
            }
        }

        return new ExamSessionResponse(
                exam.getId(),
                exam.getExamType(),
                exam.getStatus().name(),
                exam.getTotalQuestions(),
                (int) answeredCount,
                questionResponses,
                exam.getStartedAt()
        );
    }

    public void submitAnswer(UUID examId, UUID userId, SubmitAnswerRequest request) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw BusinessException.badRequest("Exam is already " + exam.getStatus());
        }

        Integer questionId = Integer.valueOf(request.questionId());
        List<ExamAnswer> answers = examAnswerRepository.findByExamIdOrderByQuestionId(examId);
        ExamAnswer answer = answers.stream()
                .filter(a -> a.getQuestionId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> BusinessException.badRequest("Question not in this exam"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> BusinessException.notFound("Question not found"));

        answer.setSelectedAnswer(request.selectedAnswer());
        answer.setIsCorrect(question.getCorrectAnswer().equals(request.selectedAnswer()));
        answer.setAnsweredAt(Instant.now());
        examAnswerRepository.save(answer);
    }

    public ExamResultResponse finishExam(UUID examId, UUID userId) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw BusinessException.badRequest("Exam is already " + exam.getStatus());
        }

        List<ExamAnswer> answers = examAnswerRepository.findByExamIdOrderByQuestionId(examId);
        int correct = (int) answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
        int wrong = answers.size() - correct;
        int score = calculateScore(correct, exam.getTotalQuestions());

        exam.setStatus(ExamStatus.COMPLETED);
        exam.setCorrectCount(correct);
        exam.setWrongCount(wrong);
        exam.setScore(score);
        exam.setCompletedAt(Instant.now());
        examRepository.save(exam);

        return buildResultResponse(exam, answers);
    }

    @Transactional(readOnly = true)
    public ExamResultResponse getExamResult(UUID examId, UUID userId) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        if (exam.getStatus() == ExamStatus.IN_PROGRESS) {
            throw BusinessException.badRequest("Exam is still in progress");
        }

        List<ExamAnswer> answers = examAnswerRepository.findByExamIdOrderByQuestionId(examId);
        return buildResultResponse(exam, answers);
    }

    @Transactional(readOnly = true)
    public Page<Exam> getExamHistory(UUID userId, Pageable pageable) {
        return examRepository.findByUserIdOrderByStartedAtDesc(userId, pageable);
    }

    private int calculateScore(int correct, int total) {
        return (int) Math.round((double) correct / total * 100);
    }

    private ExamResultResponse buildResultResponse(Exam exam, List<ExamAnswer> answers) {
        List<Question> questions = questionRepository.findAllById(exam.getQuestionIds());
        Map<Integer, Question> qMap = new HashMap<>();
        for (Question q : questions) qMap.put(q.getId(), q);

        List<java.util.Map<String, Object>> answerDetails = new ArrayList<>();
        for (ExamAnswer ans : answers) {
            Question q = qMap.get(ans.getQuestionId());
            if (q == null) continue;
            Object options;
            try {
                options = objectMapper.readValue(q.getOptions(), Object.class);
            } catch (JsonProcessingException e) {
                options = q.getOptions();
            }
            java.util.Map<String, Object> detail = new HashMap<>();
            detail.put("questionId", q.getId());
            detail.put("questionText", q.getQuestionText());
            detail.put("options", options);
            detail.put("correctAnswer", q.getCorrectAnswer());
            detail.put("selectedAnswer", ans.getSelectedAnswer() != null ? ans.getSelectedAnswer() : "");
            detail.put("isCorrect", ans.getIsCorrect() != null ? ans.getIsCorrect() : false);
            detail.put("explanation", q.getExplanation() != null ? q.getExplanation() : "");
            answerDetails.add(detail);
        }

        boolean passed = exam.getCorrectCount() >= REQUIRED_CORRECT_FOR_PASS
                && !hasWrongCritical(answers, qMap);

        return new ExamResultResponse(
                exam.getId(), exam.getExamType(), exam.getStatus().name(),
                exam.getTotalQuestions(), exam.getCorrectCount(), exam.getWrongCount(),
                exam.getScore(), passed,
                answerDetails,
                exam.getStartedAt(), exam.getCompletedAt()
        );
    }

    private boolean hasWrongCritical(List<ExamAnswer> answers, Map<Integer, Question> qMap) {
        return answers.stream().anyMatch(a -> {
            Question q = qMap.get(a.getQuestionId());
            return q != null && Boolean.TRUE.equals(q.getIsCritical())
                    && !Boolean.TRUE.equals(a.getIsCorrect());
        });
    }

    private QuestionResponse toQuestionResponse(Question q) {
        Object optionsObj;
        try {
            optionsObj = objectMapper.readValue(q.getOptions(), Object.class);
        } catch (JsonProcessingException e) {
            optionsObj = q.getOptions();
        }
        return new QuestionResponse(
                q.getId(), q.getChapter(), q.getQuestionText(),
                optionsObj, q.getIsCritical(), q.getImageUrl()
        );
    }
}
