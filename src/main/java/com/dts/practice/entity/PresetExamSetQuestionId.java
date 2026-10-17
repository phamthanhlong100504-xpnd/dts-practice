package com.dts.practice.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PresetExamSetQuestionId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID presetExamSetId;
    private Integer questionOrder;
}