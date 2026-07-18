package com.uniai.chat.infrastructure.interpretation;

import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetConfiguration;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretation;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.GraduateQueryInterpreterPromptPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiGraduateQueryInterpretationAdapterTest {

    @Test
    void interpretShouldParseValidJsonAndIgnoreUnknownFields() {
        RecordingAiServicePort aiServicePort = new RecordingAiServicePort("""
                {
                  "schemaVersion": 1,
                  "intent": "PROGRAM_LOOKUP",
                  "universities": ["AUB"],
                  "degreeTypes": ["MASTER"],
                  "detailLevel": "LIST",
                  "followUp": false,
                  "comparison": false,
                  "topicKeywords": ["programs"],
                  "ambiguous": false,
                  "clarificationNeeded": null,
                  "unsupportedConstraints": [],
                  "unexpectedField": "ignored"
                }
                """);

        AiGraduateQueryInterpretationAdapter adapter = adapter(aiServicePort);

        GraduateQueryInterpretation interpretation = adapter.interpret(new GraduateQueryInterpretationRequest("What programs does AUB offer?", List.of(), ConversationMemory.empty()));

        assertEquals(1, aiServicePort.callCount);
        assertEquals(1, interpretation.schemaVersion());
        assertEquals("PROGRAM_LOOKUP", interpretation.intent());
        assertEquals(List.of("AUB"), interpretation.universities());
        assertEquals(List.of("MASTER"), interpretation.degreeTypes());
    }

    @Test
    void interpretShouldStripMarkdownFences() {
        AiGraduateQueryInterpretationAdapter adapter = adapter(new RecordingAiServicePort("""
                ```json
                {
                  "schemaVersion": 1,
                  "intent": "TUITION_AGGREGATION",
                  "universities": ["USJ"],
                  "ambiguous": false
                }
                ```
                """));

        GraduateQueryInterpretation interpretation = adapter.interpret(new GraduateQueryInterpretationRequest("What is the tuition at USJ?", List.of(), ConversationMemory.empty()));

        assertEquals("TUITION_AGGREGATION", interpretation.intent());
        assertEquals(List.of("USJ"), interpretation.universities());
    }

    @Test
    void interpretShouldFailForBlankOrInvalidJson() {
        AiGraduateQueryInterpretationAdapter blankAdapter = adapter(new RecordingAiServicePort("   "));
        AiGraduateQueryInterpretationAdapter invalidAdapter = adapter(new RecordingAiServicePort("{ not json }"));

        assertThrows(IllegalStateException.class, () -> blankAdapter.interpret(new GraduateQueryInterpretationRequest("Hello", List.of(), ConversationMemory.empty())));
        assertThrows(IllegalStateException.class, () -> invalidAdapter.interpret(new GraduateQueryInterpretationRequest("Hello", List.of(), ConversationMemory.empty())));
    }

    @Test
    void interpretShouldMapCompactJsonContract() {
        AiGraduateQueryInterpretationAdapter adapter = adapter(new RecordingAiServicePort("""
                {"schemaVersion":1,"resource":"PROGRAM","operation":"AGGREGATE","universities":["LAU"],"degreeTypes":["MASTER"],"aggregation":"AVG","tuition":{"currency":"USD"}}
                """));

        GraduateQueryInterpretation interpretation = adapter.interpret(
                new GraduateQueryInterpretationRequest("What is tuition at LAU?", List.of(), ConversationMemory.empty()));

        assertEquals("PROGRAM", interpretation.resource());
        assertEquals("AGGREGATE", interpretation.operation());
        assertEquals("AVG", interpretation.aggregation());
        assertEquals("USD", interpretation.currency());
        assertEquals(List.of("LAU"), interpretation.universities());
    }

    @Test
    void interpretShouldRejectMaxTokenCompletion() {
        RecordingAiServicePort port = new RecordingAiServicePort("{\"schemaVersion\":1}", "MAX_TOKENS");
        AiGraduateQueryInterpretationAdapter adapter = adapter(port);
        assertThrows(IllegalStateException.class, () -> adapter.interpret(
                new GraduateQueryInterpretationRequest("What is tuition?", List.of(), ConversationMemory.empty())));
    }

    private AiGraduateQueryInterpretationAdapter adapter(RecordingAiServicePort aiServicePort) {
        return new AiGraduateQueryInterpretationAdapter(
                aiServicePort,
                () -> "Interpretation prompt",
                new GraduateQueryInterpretationBudgetConfiguration(true, 1000, 50, 4, "prompts/graduate-query-interpreter-prompt.txt"),
                new com.fasterxml.jackson.databind.ObjectMapper()
        );
    }

    private static final class RecordingAiServicePort implements AiServicePort {
        private final String content;
        private final String finishReason;
        private int callCount;

        private RecordingAiServicePort(String content) {
            this(content, null);
        }

        private RecordingAiServicePort(String content, String finishReason) {
            this.content = content;
            this.finishReason = finishReason;
        }

        @Override
        public AiResponse generateResponse(com.uniai.chat.application.dto.ai.AiRequest request) {
            callCount++;
            return AiResponse.builder()
                    .provider("gemini")
                    .model("gemini-2.5-flash")
                    .content(content)
                    .finishReason(finishReason)
                    .fallback(false)
                    .build();
        }
    }
}
