package com.uniai.chat.infrastructure.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.budget.ConversationMemoryBudgetConfiguration;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationMemoryPatch;
import com.uniai.chat.application.memory.ConversationMemoryPromptFormatter;
import com.uniai.chat.application.memory.ConversationMemoryUpdateRequest;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.ConversationMemoryPromptPort;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationResult;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeReference;
import com.uniai.chat.application.retrieval.GraduateKnowledgeReferenceKind;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiConversationMemoryUpdateAdapterTest {

    @Test
    void promptVisibleMemoryOmitsPersistenceIdentifiers() {
        ConversationMemory memory = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(new com.uniai.chat.application.memory.MemoryUniversityRef(42L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                "PROGRAM_LOOKUP",
                true,
                List.of(new com.uniai.chat.application.memory.MemoryUniversityRef(42L, "American University of Beirut", "AUB")),
                List.of(), List.of(), List.of(), new com.uniai.chat.application.memory.ConversationPreferences(null, null, null),
                List.of(new GraduateKnowledgeReference(GraduateKnowledgeReferenceKind.UNIVERSITY, "American University of Beirut", "AUB", 1)),
                List.of(), null
        );

        String prompt = ConversationMemoryPromptFormatter.render(memory);

        assertFalse(prompt.contains("42"));
        assertTrue(prompt.contains("AUB"));
        assertTrue(prompt.contains("UNIVERSITY"));
    }

    @Test
    void proposeUpdateShouldParseFencedJsonAndPreserveContextShape() {
        RecordingAiServicePort aiServicePort = new RecordingAiServicePort();
        aiServicePort.nextResponse = AiResponse.builder()
                .content("```json\n{\"schemaVersion\":1,\"setLastIntent\":\"PROGRAM_LOOKUP\",\"setComparisonActive\":false,\"replaceActiveUniversities\":[\"AUB\"],\"addActiveUniversities\":[],\"removeActiveUniversities\":[],\"replaceActiveDegreeTypes\":[\"MASTER\"],\"addActiveDegreeTypes\":[],\"removeActiveDegreeTypes\":[],\"replaceComparisonUniversities\":[],\"addPendingTopics\":[],\"removePendingTopics\":[],\"addCorrections\":[],\"removeCorrections\":[],\"setAllowedPreferences\":null,\"clearFields\":[]}\n```")
                .provider("gemini")
                .model("gemini-2.5-flash")
                .build();

        AiConversationMemoryUpdateAdapter adapter = new AiConversationMemoryUpdateAdapter(
                aiServicePort,
                promptPort(),
                new ConversationMemoryBudgetConfiguration(true, 1200, 64, "prompts/conversation-memory-updater-prompt.txt"),
                new ObjectMapper()
        );

        ConversationMemoryUpdateRequest request = new ConversationMemoryUpdateRequest(
                ConversationMemory.empty(),
                "What master's programs does AUB offer?",
                "Here is the official answer.",
                interpretationResult()
        );

        ConversationMemoryPatch patch = adapter.proposeUpdate(request);

        assertEquals("PROGRAM_LOOKUP", patch.setLastIntent());
        assertTrue(patch.replaceActiveUniversities().contains("AUB"));
        assertEquals("Conversation memory updater prompt", aiServicePort.lastRequest.getSystemPrompt());
        assertTrue(aiServicePort.lastRequest.getContext().get(0).contains("Previous trusted memory"));
        assertTrue(aiServicePort.lastRequest.getContext().get(1).contains("Validated interpretation"));
        assertTrue(aiServicePort.lastRequest.getContext().get(2).contains("Final assistant response"));
        assertEquals(64, aiServicePort.lastRequest.getMaxTokens());
    }

    @Test
    void proposeUpdateShouldRejectBlankInvalidAndFallbackResponses() {
        AiConversationMemoryUpdateAdapter blankAdapter = new AiConversationMemoryUpdateAdapter(
                new RecordingAiServicePort(AiResponse.builder().content("   ").provider("gemini").model("gemini-2.5-flash").build()),
                promptPort(),
                new ConversationMemoryBudgetConfiguration(true, 1200, 64, "prompts/conversation-memory-updater-prompt.txt"),
                new ObjectMapper()
        );

        AiConversationMemoryUpdateAdapter fallbackAdapter = new AiConversationMemoryUpdateAdapter(
                new RecordingAiServicePort(AiResponse.builder().fallback(true).content("{\"schemaVersion\":1}").provider("gemini").model("gemini-2.5-flash").build()),
                promptPort(),
                new ConversationMemoryBudgetConfiguration(true, 1200, 64, "prompts/conversation-memory-updater-prompt.txt"),
                new ObjectMapper()
        );

        AiConversationMemoryUpdateAdapter invalidAdapter = new AiConversationMemoryUpdateAdapter(
                new RecordingAiServicePort(AiResponse.builder().content("not json").provider("gemini").model("gemini-2.5-flash").build()),
                promptPort(),
                new ConversationMemoryBudgetConfiguration(true, 1200, 64, "prompts/conversation-memory-updater-prompt.txt"),
                new ObjectMapper()
        );

        ConversationMemoryUpdateRequest request = new ConversationMemoryUpdateRequest(
                ConversationMemory.empty(),
                "Hello",
                "Assistant",
                interpretationResult()
        );

        assertThrows(IllegalStateException.class, () -> blankAdapter.proposeUpdate(request));
        assertThrows(IllegalStateException.class, () -> fallbackAdapter.proposeUpdate(request));
        assertThrows(IllegalStateException.class, () -> invalidAdapter.proposeUpdate(request));
    }

    private ConversationMemoryPromptPort promptPort() {
        return () -> "Conversation memory updater prompt";
    }

    private GraduateQueryInterpretationResult interpretationResult() {
        return GraduateQueryInterpretationResult.valid(
                new GraduateKnowledgeQuery(
                        GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                        List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                        List.of("MASTER"),
                        GraduateProgramDetailLevel.LIST,
                        false,
                        false
                ),
                1,
                1
        );
    }

    private static final class RecordingAiServicePort implements AiServicePort {
        private AiRequest lastRequest;
        private AiResponse nextResponse;

        private RecordingAiServicePort() {
        }

        private RecordingAiServicePort(AiResponse response) {
            this.nextResponse = response;
        }

        @Override
        public AiResponse generateResponse(AiRequest request) {
            lastRequest = request;
            return nextResponse;
        }
    }
}
