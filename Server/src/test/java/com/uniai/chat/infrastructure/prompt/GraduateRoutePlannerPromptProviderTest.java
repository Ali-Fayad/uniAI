package com.uniai.chat.infrastructure.prompt;

import com.uniai.chat.application.planning.GraduateAiRoute;
import com.uniai.chat.application.planning.GraduateAiRouteCatalog;
import com.uniai.chat.infrastructure.config.GraduateQueryInterpretationProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateRoutePlannerPromptProviderTest {
    @Test
    void generatesEveryEnabledRouteAndItsTypedArgumentsFromJavaMetadata() {
        GraduateRoutePlannerPromptProvider provider = new GraduateRoutePlannerPromptProvider(
                new GraduateQueryInterpretationProperties(), new GraduateAiRouteCatalog());
        String prompt = provider.getPrompt();

        for (GraduateAiRoute route : GraduateAiRoute.values()) {
            assertTrue(prompt.contains(route + "|"), route.name());
        }
        assertTrue(prompt.contains("GET_PROGRAM_TUITION"));
        assertTrue(prompt.contains("programName:string!"));
        assertTrue(prompt.contains("degreeType:enum(CERTIFICATE|DIPLOMA|MASTER|PHD)?"));
        assertTrue(prompt.contains("Return exactly one JSON object"));
        assertFalse(prompt.contains("{{ROUTE_CATALOG}}"));
    }
}
