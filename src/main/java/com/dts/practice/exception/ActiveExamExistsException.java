package com.dts.practice.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ActiveExamExistsException extends BusinessException {

    private final UUID activeExamId;
    private final UUID activePresetExamSetId;
    private final String activeStatus;

    public ActiveExamExistsException(
            String licenseClass,
            UUID activeExamId,
            UUID activePresetExamSetId,
            String activeStatus
    ) {
        super(
                "ACTIVE_EXAM_EXISTS",
                "Bạn đang có một bài " + licenseClass
                        + " chưa hoàn thành.",
                HttpStatus.CONFLICT
        );
        this.activeExamId = activeExamId;
        this.activePresetExamSetId = activePresetExamSetId;
        this.activeStatus = activeStatus;
    }

    public UUID getActiveExamId() {
        return activeExamId;
    }

    public UUID getActivePresetExamSetId() {
        return activePresetExamSetId;
    }

    public String getActiveStatus() {
        return activeStatus;
    }
}