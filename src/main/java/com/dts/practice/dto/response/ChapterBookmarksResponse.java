package com.dts.practice.dto.response;

import java.util.List;

public record ChapterBookmarksResponse(
        int chapterId,
        String licenseClass,
        List<Integer> questionIds
) {}