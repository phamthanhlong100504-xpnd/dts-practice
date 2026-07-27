package com.dts.practice.controller;

import com.dts.practice.entity.Question;
import com.dts.practice.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("QuestionController Integration")
class QuestionControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestionRepository questionRepository;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();

        Question q = Question.builder()
                .id(1).chapter(1)
                .questionText("What does this sign mean?")
                .options("{\"A\":\"Stop\",\"B\":\"Go\",\"C\":\"Yield\",\"D\":\"Turn\"}")
                .correctAnswer("A").isCritical(false)
                .explanation("The stop sign means you must come to a complete stop.")
                .imageUrl(null)
                .build();
        questionRepository.save(q);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/questions/{id} should return question")
    void getById_shouldReturnQuestion() throws Exception {
        mockMvc.perform(get("/api/v1/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.chapter").value(1))
                .andExpect(jsonPath("$.data.questionText").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/questions/{id} should return 404 for missing question")
    void getById_notFound_shouldReturnError() throws Exception {
        mockMvc.perform(get("/api/v1/questions/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/questions/chapter/{chapter} should return questions")
    void getByChapter_shouldReturnQuestions() throws Exception {
        mockMvc.perform(get("/api/v1/questions/chapter/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/questions/critical should return critical questions")
    void getCritical_shouldReturnEmptyListWhenNone() throws Exception {
        mockMvc.perform(get("/api/v1/questions/critical"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/questions/stats should return question statistics")
    void getStats_shouldReturnStats() throws Exception {
        mockMvc.perform(get("/api/v1/questions/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1));
    }
}
