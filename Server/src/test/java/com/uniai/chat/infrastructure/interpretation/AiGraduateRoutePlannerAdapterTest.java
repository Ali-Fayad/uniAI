package com.uniai.chat.infrastructure.interpretation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetConfiguration;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationProviderException;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.planning.GraduateAiRoute;
import com.uniai.chat.application.planning.GraduateAiRouteCatalog;
import com.uniai.chat.application.planning.GraduateRouteArguments;
import com.uniai.chat.application.planning.GraduateRoutePlanParser;
import com.uniai.chat.application.planning.ValidatedGraduateRoutePlan;
import com.uniai.chat.application.port.out.AiServicePort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiGraduateRoutePlannerAdapterTest {
    @Test
    void returnsAValidatedRouteSpecificPlan() {
        RecordingAiService provider = new RecordingAiService(
                "{\"route\":\"GET_PROGRAM_TUITION\",\"arguments\":{\"university\":\"AUB\",\"programName\":\"Computer Science\"}}",
                "STOP");
        AiGraduateRoutePlannerAdapter adapter = adapter(provider);

        ValidatedGraduateRoutePlan<?> plan = adapter.plan(request("What is Computer Science tuition at AUB?"));

        assertEquals(GraduateAiRoute.GET_PROGRAM_TUITION, plan.route());
        assertInstanceOf(GraduateRouteArguments.ProgramTuitionArguments.class, plan.arguments());
        assertEquals(500, provider.lastRequest.getMaxTokens());
        assertEquals(0.0, provider.lastRequest.getTemperature());
    }

    @Test
    void rejectsMarkdownUnknownArgumentsAndMalformedJson() {
        assertInvalid("```json\n{\"route\":\"LIST_PROGRAMS\",\"arguments\":{}}\n```");
        assertInvalid("{\"route\":\"LIST_PROGRAMS\",\"arguments\":{\"sql\":\"select 1\"}}");
        assertInvalid("{broken");
    }

    @Test
    void classifiesProviderTruncationSeparately() {
        GraduateQueryInterpretationProviderException exception = assertThrows(
                GraduateQueryInterpretationProviderException.class,
                () -> adapter(new RecordingAiService("{}", "MAX_TOKENS")).plan(request("List programs")));
        assertEquals("AI_QUERY_PLANNER_PROVIDER_TRUNCATED", exception.failureCategory());
    }

    private void assertInvalid(String content) {
        assertThrows(GraduateQueryInterpretationProviderException.class,
                () -> adapter(new RecordingAiService(content, "STOP")).plan(request("query")));
    }

    private AiGraduateRoutePlannerAdapter adapter(RecordingAiService service) {
        GraduateRoutePlanParser parser = new GraduateRoutePlanParser(new GraduateAiRouteCatalog(), new ObjectMapper());
        return new AiGraduateRoutePlannerAdapter(
                service,
                () -> "Strict planner prompt",
                new GraduateQueryInterpretationBudgetConfiguration(true, 4500, 500, 4,
                        "prompts/graduate-route-planner-prompt.txt"),
                parser);
    }

    private GraduateQueryInterpretationRequest request(String message) {
        return new GraduateQueryInterpretationRequest(message, List.of(), ConversationMemory.empty());
    }

    private static final class RecordingAiService implements AiServicePort {
        private final String content;
        private final String finishReason;
        private AiRequest lastRequest;

        private RecordingAiService(String content, String finishReason) {
            this.content = content;
            this.finishReason = finishReason;
        }

        @Override
        public AiResponse generateResponse(AiRequest request) {
            lastRequest = request;
            return AiResponse.builder()
                    .provider("gemini")
                    .model("test-model")
                    .content(content)
                    .finishReason(finishReason)
                    .fallback(false)
                    .build();
        }
    }
}
