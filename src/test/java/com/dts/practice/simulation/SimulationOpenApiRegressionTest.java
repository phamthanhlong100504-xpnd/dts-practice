package com.dts.practice.simulation;
import com.dts.practice.controller.ChapterBookmarkController;
import com.dts.practice.controller.ChapterLearnedAnswerQueryController;
import com.dts.practice.controller.ChapterLearningController;
import com.dts.practice.controller.ChapterReviewSessionController;
import com.dts.practice.controller.CriticalAnswerController;
import com.dts.practice.controller.CriticalCursorController;
import com.dts.practice.controller.CriticalQuestionProgressController;
import com.dts.practice.controller.CriticalSessionController;
import com.dts.practice.controller.ExamConfigController;
import com.dts.practice.controller.ExamController;
import com.dts.practice.controller.ExamCursorController;
import com.dts.practice.controller.ExamProgressReceiptController;
import com.dts.practice.controller.ExamQuestionFlagController;
import com.dts.practice.controller.ExamRetryController;
import com.dts.practice.controller.HomeController;
import com.dts.practice.controller.LearningBankCatalogController;
import com.dts.practice.controller.LicenseCatalogController;
import com.dts.practice.controller.MemoryTipController;
import com.dts.practice.controller.PresetExamSetController;
import com.dts.practice.controller.QuestionBankImportController;
import com.dts.practice.controller.QuestionController;
import com.dts.practice.controller.ReferenceContentBankImportController;
import com.dts.practice.controller.TrafficSignController;
import com.dts.practice.controller.WrongQuestionController;
import com.dts.practice.controller.WrongReviewSessionController;
import com.dts.practice.service.ActiveExamService;
import com.dts.practice.service.ChapterBookmarkService;
import com.dts.practice.service.ChapterLearnedAnswerQueryService;
import com.dts.practice.service.ChapterLearningService;
import com.dts.practice.service.ChapterReviewSessionService;
import com.dts.practice.service.CriticalAnswerService;
import com.dts.practice.service.CriticalCursorService;
import com.dts.practice.service.CriticalQuestionProgressService;
import com.dts.practice.service.CriticalSessionService;
import com.dts.practice.service.ExamCursorService;
import com.dts.practice.service.ExamProgressReceiptService;
import com.dts.practice.service.ExamQuestionFlagService;
import com.dts.practice.service.ExamRetryService;
import com.dts.practice.service.ExamService;
import com.dts.practice.service.ExamSessionFeedbackService;
import com.dts.practice.service.LearningBankCatalogService;
import com.dts.practice.service.LicenseCatalogService;
import com.dts.practice.service.MemoryTipBankImportService;
import com.dts.practice.service.MemoryTipReadService;
import com.dts.practice.service.PresetExamSetService;
import com.dts.practice.service.QuestionBankImportService;
import com.dts.practice.service.QuestionService;
import com.dts.practice.service.TrafficSignBankImportService;
import com.dts.practice.service.TrafficSignReadService;
import com.dts.practice.service.WrongQuestionService;
import com.dts.practice.service.WrongReviewFinishService;
import com.dts.practice.service.WrongReviewInteractionService;
import com.dts.practice.service.WrongReviewSessionService;
import com.dts.practice.simulation.SimulationBookmarkController;
import com.dts.practice.simulation.SimulationBookmarkService;
import com.dts.practice.simulation.SimulationCatalogController;
import com.dts.practice.simulation.SimulationCatalogService;
import com.dts.practice.simulation.SimulationExamController;
import com.dts.practice.simulation.SimulationExamService;
import com.dts.practice.simulation.SimulationExamShortcutsController;
import com.dts.practice.simulation.SimulationLearningController;
import com.dts.practice.simulation.SimulationLearningReads;
import com.dts.practice.simulation.SimulationLearningService;
import com.dts.practice.simulation.SimulationLearningShortcutsController;
import com.dts.practice.simulation.SimulationLicenseCapabilities;
import com.dts.practice.simulation.SimulationPresetController;
import com.dts.practice.simulation.SimulationPresetService;
import com.dts.practice.simulation.SimulationReplayService;
import com.dts.practice.simulation.SimulationReviewController;
import com.dts.practice.simulation.SimulationReviewService;
import com.dts.practice.simulation.SimulationShortcutService;
import com.dts.practice.config.OpenApiConfig;
import com.dts.practice.config.SecurityConfig;
import com.dts.practice.config.JwtValidationProperties;
import com.dts.practice.security.JwtValidationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springdoc.core.configuration.*;
import org.springdoc.webmvc.core.configuration.*;
import org.springdoc.webmvc.ui.SwaggerConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;
@WebMvcTest(properties={"springdoc.api-docs.enabled=true","springdoc.api-docs.path=/api-docs"})
@ImportAutoConfiguration({org.springdoc.core.properties.SpringDocConfigProperties.class,org.springdoc.core.properties.SwaggerUiConfigProperties.class,org.springdoc.core.properties.SwaggerUiOAuthProperties.class,SpringDocConfiguration.class,SpringDocSpecPropertiesConfiguration.class,SpringDocWebMvcConfiguration.class,SwaggerConfig.class})
@org.springframework.test.context.ContextConfiguration(classes={SimulationReviewController.class,SimulationPresetController.class,SimulationBookmarkController.class,SimulationLearningShortcutsController.class,SimulationLearningController.class,SimulationExamController.class,SimulationExamShortcutsController.class,SimulationCatalogController.class,LearningBankCatalogController.class,HomeController.class,QuestionBankImportController.class,CriticalAnswerController.class,QuestionController.class,ExamController.class,CriticalQuestionProgressController.class,ExamProgressReceiptController.class,CriticalCursorController.class,PresetExamSetController.class,ReferenceContentBankImportController.class,ChapterBookmarkController.class,ExamConfigController.class,WrongReviewSessionController.class,MemoryTipController.class,ExamCursorController.class,LicenseCatalogController.class,ChapterLearnedAnswerQueryController.class,CriticalSessionController.class,WrongQuestionController.class,ExamQuestionFlagController.class,ChapterReviewSessionController.class,ExamRetryController.class,ChapterLearningController.class,TrafficSignController.class,SecurityConfig.class,JwtValidationFilter.class,com.dts.practice.exception.GlobalExceptionHandler.class,SimulationLearningAdvice.class,SimulationExamAdvice.class,SimulationShortcutAdvice.class,SimulationReviewAdvice.class,SimulationBookmarkAdvice.class,OpenApiConfig.class,SimulationOpenApiRegressionTest.Props.class})
@MockBean(classes={ActiveExamService.class,ChapterBookmarkService.class,ChapterLearnedAnswerQueryService.class,ChapterLearningService.class,ChapterReviewSessionService.class,CriticalAnswerService.class,CriticalCursorService.class,CriticalQuestionProgressService.class,CriticalSessionService.class,ExamCursorService.class,ExamProgressReceiptService.class,ExamQuestionFlagService.class,ExamRetryService.class,ExamService.class,ExamSessionFeedbackService.class,LearningBankCatalogService.class,LicenseCatalogService.class,MemoryTipBankImportService.class,MemoryTipReadService.class,PresetExamSetService.class,QuestionBankImportService.class,QuestionService.class,SimulationBookmarkService.class,SimulationCatalogService.class,SimulationExamService.class,SimulationLearningReads.class,SimulationLearningService.class,SimulationLicenseCapabilities.class,SimulationPresetService.class,SimulationReplayService.class,SimulationReviewService.class,SimulationShortcutService.class,TrafficSignBankImportService.class,TrafficSignReadService.class,WrongQuestionService.class,WrongReviewFinishService.class,WrongReviewInteractionService.class,WrongReviewSessionService.class})
class SimulationOpenApiRegressionTest {
 @TestConfiguration static class Props{@Bean JwtValidationProperties jwtValidationProperties(){return SimulationFixtures.PROPS;}}
 @Autowired MockMvc mvc;
 @org.springframework.beans.factory.annotation.Value("${springdoc.api-docs.path}") String docsPath;

 @Test void configuredDocumentIncludesAllShortcutOperations()throws Exception{
  var reply=mvc.perform(get(docsPath)).andReturn();
  if(reply.getResolvedException()!=null)reply.getResolvedException().printStackTrace();
  assertEquals(200,reply.getResponse().getStatus());
  var document=new com.fasterxml.jackson.databind.ObjectMapper().readTree(reply.getResponse().getContentAsString());assertTrue(document.path("openapi").asText().startsWith("3."));
  var paths=document.path("paths");
  assertTrue(paths.path("/api/v1/simulations/attempts/{attemptId}/shortcuts").has("get"));
  assertTrue(paths.path("/api/v1/simulations/attempts/{attemptId}/shortcuts/enable").has("post"));
  assertTrue(paths.path("/api/v1/simulations/attempts/{attemptId}/shortcuts/skip-intro").has("post"));
  assertTrue(paths.path("/api/v1/simulation-exams/sessions/{sessionId}/shortcuts").has("get"));
  assertTrue(paths.path("/api/v1/simulation-exams/sessions/{sessionId}/shortcuts/enable").has("post"));
  assertTrue(paths.path("/api/v1/simulation-exams/sessions/{sessionId}/shortcuts/skip-intro").has("post"));
  assertTrue(paths.path("/api/v1/simulation-exams/sessions/{sessionId}/shortcuts/advance-after-reaction").has("post"));
  assertTrue(paths.has("/api/v1/exams/{examId}/result"));System.out.println("Generated OpenAPI paths="+paths.size());
 }
 @Test void unmappedDocsVariantIs404RatherThanInternal500()throws Exception{var r=mvc.perform(get(docsPath.equals("/api-docs")?"/v3/api-docs":"/api-docs")).andReturn();assertEquals(404,r.getResponse().getStatus());}
 @Test void swaggerConfigUsesConfiguredDocumentPath()throws Exception{var r=mvc.perform(get(docsPath+"/swagger-config")).andReturn();assertEquals(200,r.getResponse().getStatus());var d=new com.fasterxml.jackson.databind.ObjectMapper().readTree(r.getResponse().getContentAsString());assertEquals(docsPath,d.path("url").asText());}
 @Test void swaggerUiEntryRedirectsToActualUi()throws Exception{var r=mvc.perform(get("/swagger-ui.html")).andReturn();assertEquals(302,r.getResponse().getStatus());assertTrue(r.getResponse().getHeader("Location").contains("/swagger-ui/index.html"));}
 @Test void missingDocsVariantNeverChangesAuthenticatedRouteProtection()throws Exception{assertEquals(401,mvc.perform(get("/api/v1/simulation-exams/sessions/active").param("licenseClass","B")).andReturn().getResponse().getStatus());}
 @Test void missingSwaggerAssetIs404WithoutLeakingException()throws Exception{var r=mvc.perform(get("/swagger-ui/no-such-file.js")).andReturn();assertEquals(404,r.getResponse().getStatus());assertFalse(r.getResponse().getContentAsString().contains("NoResourceFoundException"));}
}
