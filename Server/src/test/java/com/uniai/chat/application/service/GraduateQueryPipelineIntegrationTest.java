package com.uniai.chat.application.service;

import com.uniai.chat.application.dto.ai.AiOperation;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.dto.command.SendMessageCommand;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.support.PostgresIntegrationTest;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Text-to-context coverage using the real interpreter, routing, SQL adapters,
 * PostgreSQL, and context formatter. External provider calls are recorded stubs.
 */
@SpringBootTest(properties = {
        "ai.provider=gemini",
        "ai.gemini.api-key=test-key",
        "conversation.memory.enabled=false"
})
@Import(GraduateQueryPipelineIntegrationTest.TestConfig.class)
class GraduateQueryPipelineIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private ChatApplicationService chatApplicationService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RecordingAiServicePort aiServicePort;

    @BeforeEach
    void resetFixture() {
        jdbcTemplate.update("DELETE FROM campus WHERE university_id IN (SELECT id FROM university WHERE acronym = 'PIP')");
        jdbcTemplate.update("DELETE FROM university WHERE acronym = 'PIP'");
        aiServicePort.reset();
    }

    @Test
    void deterministicTextToSqlPipelinePreservesAggregateContext() {
        Long universityId = seedLocationFixture();
        User user = userRepository.save(user("pipeline-count@example.com", "pipeline-count"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        chatApplicationService.sendMessage(user.getEmail(), SendMessageCommand.builder()
                .chatId(chat.getId())
                .content("How many campuses are in Beirut?")
                .build());

        assertEquals(0, aiServicePort.interpretationCalls,
                "high-confidence location count should bypass the provider");
        assertEquals(1, aiServicePort.mainCalls);
        assertTrue(aiServicePort.lastMainRequest.getContext().stream()
                .anyMatch(context -> context.contains("Resource: CAMPUS")
                        && context.contains("Operation: COUNT")
                        && context.contains("Total: 1")),
                () -> "Expected structured count context, universityId=" + universityId
                        + ": " + aiServicePort.lastMainRequest.getContext());
    }

    @Test
    void deterministicTextToSqlPipelinePreservesCampusEvidence() {
        seedLocationFixture();
        User user = userRepository.save(user("pipeline-exists@example.com", "pipeline-exists"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        chatApplicationService.sendMessage(user.getEmail(), SendMessageCommand.builder()
                .chatId(chat.getId())
                .content("Does PIP have a campus in Beirut?")
                .build());

        assertEquals(0, aiServicePort.interpretationCalls);
        assertTrue(aiServicePort.lastMainRequest.getContext().stream()
                .anyMatch(context -> context.contains("Checked entity: CAMPUS")
                        && context.contains("Condition: city=Beirut")
                        && context.contains("Exists: true")
                        && context.contains("Campus: Pipeline Main Campus")
                        && context.contains("Campus type: Main")),
                () -> "Expected structured existence context: " + aiServicePort.lastMainRequest.getContext());
    }

    @Test
    void providerInterpretationStillFlowsThroughValidationAndSql() {
        seedLocationFixture();
        User user = userRepository.save(user("pipeline-provider@example.com", "pipeline-provider"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        chatApplicationService.sendMessage(user.getEmail(), SendMessageCommand.builder()
                .chatId(chat.getId())
                .content("Please provide a nuanced assessment of campus availability from the official records.")
                .build());

        assertEquals(1, aiServicePort.interpretationCalls);
        assertTrue(aiServicePort.lastMainRequest.getContext().stream()
                .anyMatch(context -> context.contains("Resource: CAMPUS")
                        && context.contains("Operation: EXISTS")
                        && context.contains("Exists: true")),
                () -> "Expected validated provider decision to reach SQL: " + aiServicePort.lastMainRequest.getContext());
    }

    private Long seedLocationFixture() {
        jdbcTemplate.update("INSERT INTO university (name, acronym, country) VALUES (?, ?, ?)",
                "Pipeline Integration University", "PIP", "Lebanon");
        Long universityId = jdbcTemplate.queryForObject(
                "SELECT id FROM university WHERE acronym = 'PIP' ORDER BY id DESC LIMIT 1", Long.class);
        jdbcTemplate.update("INSERT INTO campus (university_id, name, city, campus_type) VALUES (?, ?, ?, ?)",
                universityId, "Pipeline Main Campus", "Beirut", "Main");
        return universityId;
    }

    private User user(String email, String username) {
        return User.builder()
                .email(email)
                .username(username)
                .password("Password123!")
                .isVerified(true)
                .build();
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        RecordingAiServicePort recordingAiServicePort() {
            return new RecordingAiServicePort();
        }
    }

    static final class RecordingAiServicePort implements AiServicePort {
        private int interpretationCalls;
        private int mainCalls;
        private AiRequest lastMainRequest;

        @Override
        public AiResponse generateResponse(AiRequest request) {
            AiOperation operation = request == null ? AiOperation.UNKNOWN : request.getOperation();
            if (operation == AiOperation.INTERPRETATION) {
                interpretationCalls++;
                String message = request.getUserMessage() == null ? "" : request.getUserMessage().toLowerCase();
                String interpretation = message.contains("nuanced")
                        ? "{\"schemaVersion\":1,\"resource\":\"CAMPUS\",\"operation\":\"EXISTS\"}"
                        : "{\"schemaVersion\":1,\"resource\":\"CAMPUS\",\"operation\":\"COUNT\"}";
                return response(interpretation);
            }
            if (operation == AiOperation.MAIN_RESPONSE) {
                mainCalls++;
                lastMainRequest = request;
                return response("Structured pipeline answer.");
            }
            if (operation == AiOperation.TITLE_GENERATION) {
                return response("Pipeline test");
            }
            return response("{\"schemaVersion\":1,\"setLastIntent\":\"LOCATION_LOOKUP\",\"clearFields\":[]}");
        }

        private AiResponse response(String content) {
            return AiResponse.builder()
                    .provider("gemini")
                    .model("test-model")
                    .content(content)
                    .fallback(false)
                    .build();
        }

        private void reset() {
            interpretationCalls = 0;
            mainCalls = 0;
            lastMainRequest = null;
        }
    }
}
