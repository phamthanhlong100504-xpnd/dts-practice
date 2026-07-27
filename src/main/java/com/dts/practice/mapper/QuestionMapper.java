package com.dts.practice.mapper;

import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.entity.Question;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class QuestionMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "options", source = "options", qualifiedByName = "parseJsonOptions")
    public abstract QuestionResponse toResponse(Question entity);

    public abstract List<QuestionResponse> toResponseList(List<Question> entities);

    @Named("parseJsonOptions")
    protected Object parseJsonOptions(String optionsJson) {
        if (optionsJson == null) return null;
        try {
            return objectMapper.readValue(optionsJson, Object.class);
        } catch (JsonProcessingException e) {
            return optionsJson;
        }
    }
}
