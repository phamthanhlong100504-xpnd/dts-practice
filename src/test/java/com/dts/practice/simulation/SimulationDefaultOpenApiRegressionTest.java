package com.dts.practice.simulation;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
@WebMvcTest(properties={"springdoc.api-docs.enabled=true","springdoc.api-docs.path=/v3/api-docs"})
class SimulationDefaultOpenApiRegressionTest extends SimulationOpenApiRegressionTest {
 @Test void defaultDocumentPathWasActuallyConfigured(){assertEquals("/v3/api-docs",docsPath);}
}
