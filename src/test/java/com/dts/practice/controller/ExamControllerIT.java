package com.dts.practice.controller;

import com.dts.practice.entity.Exam;
import com.dts.practice.entity.Question;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.repository.ExamRepository;
import com.dts.practice.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("ExamController Integration")
class ExamControllerIT {

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

    @Autowired
    private ExamRepository examRepository;

    @BeforeEach
    void setUp() {
        examRepository.deleteAll();
        questionRepository.deleteAll();

        // Seed some questions
        for (int i = 1; i <= 30; i++) {
            Question q = Question.builder()
                    .id(i).chapter((i % 6) + 1)
                    .questionText("Question " + i + "?")
                    .options("{\"A\":\"Opt A\",\"B\":\"Opt B\",\"C\":\"Opt C\",\"D\":\"Opt D\"}")
                    .correctAnswer("A").isCritical(i <= 3)
                    .explanation("Explanation for Q" + i)
                    .build();
            questionRepository.save(q);
        }
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/exams should create new exam and return session")
    void startExam_shouldReturnSession() throws Exception {
        String body = """
                {
                    "examType": "A1",
                    "totalQuestions": 5,
                    "durationMinutes": 20,
                    "mode": "EXAM"
                }
                """;

        mockMvc.perform(post("/api/v1/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.examId").exists())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.totalQuestions").value(5))
                .andExpect(jsonPath("$.data.mode").value("EXAM"))
                .andExpect(jsonPath("$.data.questions.length()").value(5));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/exams should reject invalid mode")
    void startExam_shouldRejectInvalidMode() throws Exception {
        String body = """
                {
                    "examType": "A1",
                    "totalQuestions": 5,
                    "durationMinutes": 20,
                    "mode": "INVALID"
                }
                """;

        mockMvc.perform(post("/api/v1/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/exams/{id} should return 404 for non-existent exam")
    void getSession_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/exams/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/exams/leaderboard should return list")
    void getLeaderboard_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/exams/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/exams/history should return paginated history")
    void getHistory_shouldReturnPaginatedHistory() throws Exception {
        mockMvc.perform(get("/api/v1/exams/history")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
