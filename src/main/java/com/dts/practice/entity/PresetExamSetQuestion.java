package com.dts.practice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "preset_exam_set_questions")
@IdClass(PresetExamSetQuestionId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresetExamSetQuestion {

    @Id
    @Column(name = "preset_exam_set_id", nullable = false)
    private UUID presetExamSetId;

    @Id
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    // ID nội bộ của Practice.questions; không phải source_question_id.
    @Column(name = "question_id", nullable = false)
    private Integer questionId;
}