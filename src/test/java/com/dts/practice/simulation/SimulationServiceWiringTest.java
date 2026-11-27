package com.dts.practice.simulation;

import com.dts.practice.service.LearningProgressEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulationServiceWiringTest {

    @Test
    @DisplayName("Spring instantiates SimulationExamService and SimulationLearningService with @Autowired constructors cleanly")
    void springCanInstantiateMultiConstructorSimulationServices() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(AutowiredAnnotationBeanPostProcessor.class);
            ctx.registerBean(SimulationExamAccess.class, () -> mock(SimulationExamAccess.class));
            ctx.registerBean(SimulationAccess.class, () -> mock(SimulationAccess.class));
            ctx.registerBean(SimulationBankClient.class, () -> mock(SimulationBankClient.class));
            ctx.registerBean(SimulationExamRepository.class, () -> mock(SimulationExamRepository.class));
            ctx.registerBean(SimulationLearningRepository.class, () -> mock(SimulationLearningRepository.class));
            ctx.registerBean(SimulationExamMediaClient.class, () -> mock(SimulationExamMediaClient.class));
            ctx.registerBean(SimulationPlaybackClient.class, () -> mock(SimulationPlaybackClient.class));
            ctx.registerBean(SimulationExamRules.class, SimulationExamRules::new);
            ctx.registerBean(SimulationLearningClock.class, SimulationLearningClock::new);
            ctx.registerBean(LearningProgressEventPublisher.class, () -> mock(LearningProgressEventPublisher.class));
            ctx.register(SimulationExamService.class, SimulationLearningService.class);
            ctx.refresh();

            SimulationExamService examService = ctx.getBean(SimulationExamService.class);
            SimulationLearningService learningService = ctx.getBean(SimulationLearningService.class);

            assertNotNull(examService);
            assertNotNull(learningService);
            assertNotNull(ReflectionTestUtils.getField(examService, "learningProgressPublisher"));
            assertNotNull(ReflectionTestUtils.getField(learningService, "learningProgressPublisher"));
        }
    }
}
