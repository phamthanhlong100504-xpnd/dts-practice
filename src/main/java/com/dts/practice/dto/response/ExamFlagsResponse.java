package com.dts.practice.dto.response;

import java.util.List;
import java.util.UUID;

public record ExamFlagsResponse(
        UUID examId,
        List<Integer> flaggedQuestionIds
) {}