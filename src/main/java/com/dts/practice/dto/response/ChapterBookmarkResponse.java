package com.dts.practice.dto.response;

public record ChapterBookmarkResponse(
        int chapterId,
        Integer questionId,
        String licenseClass,
        boolean bookmarked
) {}