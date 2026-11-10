package com.dts.practice.adminpresets;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class AdminPresetAccessTest {
    @Test void allEndpointsAreAdminAndSubjectCapabilityGated(){
        for(Method method:AdminPresetController.class.getDeclaredMethods()){
            if(method.getName().equals("noStore"))continue;
            var guard=method.getAnnotation(PreAuthorize.class);
            assertNotNull(guard,method.getName());
            assertTrue(guard.value().contains("hasRole('ADMIN')"),method.getName());
            assertTrue(guard.value().contains("PERM_practice-exam-sets:"),method.getName());
        }
    }
    @Test void noPublishArchiveOrLearnerWriteRoutes(){
        var methods=Arrays.stream(AdminPresetController.class.getDeclaredMethods()).toList();
        assertTrue(methods.stream().noneMatch(m->m.getName().matches("(?i).*(publish|activate|archive|score|result).*")));
        for(Method method:methods){
            var post=method.getAnnotation(PostMapping.class);
            if(post!=null)for(String route:post.value())
                assertFalse(route.matches(".*(publish|activate|archive).*"));
        }
        assertEquals("/api/v1/exams/admin",AdminPresetController.class.getAnnotation(RequestMapping.class).value()[0]);
    }
}
