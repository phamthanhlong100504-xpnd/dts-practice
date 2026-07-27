package com.dts.practice.mapper;

import com.dts.practice.dto.response.ExamHistoryResponse;
import com.dts.practice.dto.response.ExamResultResponse;
import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.entity.Exam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class})
public interface ExamMapper {

    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "status", expression = "java(exam.getStatus().name())")
    @Mapping(target = "answeredCount", expression = "java(answeredCount)")
    ExamSessionResponse toSessionResponse(Exam exam, int answeredCount, List<QuestionResponse> questions);

    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "status", expression = "java(exam.getStatus().name())")
    @Mapping(target = "passed", ignore = true)
    ExamResultResponse toResultResponse(Exam exam, Object answerDetails);

    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "status", expression = "java(exam.getStatus().name())")
    @Mapping(target = "passed", ignore = true)
    ExamHistoryResponse toHistoryResponse(Exam exam);
}
