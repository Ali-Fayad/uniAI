package com.uniai.chat.application.service;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import com.uniai.chat.application.budget.AiContextBudgetConfiguration;
import com.uniai.chat.application.budget.AiContextBudgetManager;
import com.uniai.chat.application.budget.AiTokenEstimator;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetManager;
import com.uniai.chat.application.citation.GraduateCitation;
import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.dto.command.SendMessageCommand;
import com.uniai.chat.application.dto.response.MessageResponseDto;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretation;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationValidator;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationMemoryManager;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.ChatSystemPromptPort;
import com.uniai.chat.application.port.out.GraduateQueryInterpretationPort;
import com.uniai.chat.application.port.out.GraduateKnowledgeRetrievalPort;
import com.uniai.chat.application.port.out.GraduateQueryInterpreterPromptPort;
import com.uniai.chat.application.provider.AiProviderFailureCategory;
import com.uniai.chat.application.retrieval.GraduateFollowUpResolver;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQueryInterpreter;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import com.uniai.chat.application.title.ChatTitleGenerationManager;
import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.model.Message;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.chat.infrastructure.retrieval.SqlGraduateKnowledgeRetrievalAdapter;
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

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "ai.provider=gemini",
        "ai.gemini.api-key=test-key",
        "ai.groq.api-key=test-key"
})
@Import(ChatApplicationServiceIntegrationTest.TestConfig.class)
class ChatApplicationServiceIntegrationTest extends PostgresIntegrationTest {

    private static final String MAIN_SUCCESS_CONTENT = "Here is the official answer. [S1]";
    private static final String MAIN_FALLBACK_CONTENT = "AI service error : this message is from ChatApplicationService. Please try again later.";
    private static final String TITLE_CONTENT = "Graduate Programs";
    private static final String MEMORY_PATCH = """
            {
              "schemaVersion": 1,
              "setLastIntent": "PROGRAM_LOOKUP",
              "setComparisonActive": false,
              "replaceActiveUniversities": [],
              "addActiveUniversities": [],
              "removeActiveUniversities": [],
              "replaceActiveDegreeTypes": [],
              "addActiveDegreeTypes": [],
              "removeActiveDegreeTypes": [],
              "replaceComparisonUniversities": [],
              "addPendingTopics": [],
              "removePendingTopics": [],
              "addCorrections": [],
              "removeCorrections": [],
              "setAllowedPreferences": null,
              "clearFields": []
            }
            """;

    @Autowired
    private ChatApplicationService chatApplicationService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RoutingAiServicePort aiServicePort;

    @Autowired
    private ControlledGraduateQueryInterpretationPort interpretationPort;

    @Autowired
    private CountingGraduateKnowledgeRetrievalPort retrievalPort;

    @Autowired
    private ChatSystemPromptPort chatSystemPromptPort;

    @Autowired
    private GraduateQueryInterpreterPromptPort graduateQueryInterpreterPromptPort;

    @Autowired
    private GraduateQueryInterpretationBudgetManager graduateQueryInterpretationBudgetManager;

    @Autowired
    private GraduateQueryInterpretationValidator graduateQueryInterpretationValidator;

    @Autowired
    private GraduateKnowledgeQueryInterpreter graduateKnowledgeQueryInterpreter;

    @Autowired
    private GraduateFollowUpResolver graduateFollowUpResolver;

    @Autowired
    private ConversationMemoryManager conversationMemoryManager;

    @Autowired
    private ChatTitleGenerationManager chatTitleGenerationManager;

    @Autowired
    private UniversityCatalogRepository universityCatalogRepository;

    @Autowired
    private AiTokenEstimator aiTokenEstimator;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE messages, chats, users
                RESTART IDENTITY CASCADE
                """);
        aiServicePort.reset();
        interpretationPort.reset();
        retrievalPort.reset();
    }

    @Test
    void sendMessageShouldPersistSuccessfulProgramLookupWithRealDatabaseRetrieval() {
        ProgramFixture fixture = discoverProgramFixture();
        interpretationPort.nextInterpretation = programInterpretation(fixture, true);

        User user = userRepository.save(user("lookup@example.com", "lookup-user"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder()
                        .chatId(chat.getId())
                        .content("Tell me about " + fixture.universityMention())
                        .build()
        );

        assertEquals(MAIN_SUCCESS_CONTENT, result.getContent());
        assertEquals(1, result.getCitations().size());
        assertEquals("S1", result.getCitations().get(0).label());
        assertEquals(fixture.universityName(), result.getCitations().get(0).universityName());
        assertEquals(1, retrievalPort.callCount);
        assertEquals(GraduateKnowledgeIntent.PROGRAM_LOOKUP, retrievalPort.lastQuery.intent());
        assertEquals(1, aiServicePort.mainCallCount);
        assertEquals(1, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());

        Chat persistedChat = chatRepository.findById(chat.getId()).orElseThrow();
        assertNotNull(persistedChat.getConversationMemory());
        assertEquals(1L, persistedChat.getMemoryVersion());
        assertNotNull(persistedChat.getTitle());
        assertEquals(TITLE_CONTENT, persistedChat.getTitle());
        assertTrue(aiServicePort.memoryCallCount >= 1);
        assertTrue(aiServicePort.titleCallCount >= 1);
    }

    @Test
    void sendMessageShouldHandleTuitionQueryWithRealDatabaseRetrieval() {
        ProgramFixture fixture = discoverProgramFixture();
        interpretationPort.nextInterpretation = tuitionInterpretation(fixture);

        User user = userRepository.save(user("tuition@example.com", "tuition-user"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder()
                        .chatId(chat.getId())
                        .content("How much is tuition for " + fixture.universityMention() + "?")
                        .build()
        );

        assertEquals(MAIN_SUCCESS_CONTENT, result.getContent());
        assertEquals(1, result.getCitations().size());
        assertEquals("S1", result.getCitations().get(0).label());
        assertEquals(GraduateKnowledgeIntent.TUITION_AGGREGATION, retrievalPort.lastQuery.intent());
        assertEquals(fixture.degreeTypeCode(), retrievalPort.lastQuery.degreeTypes().get(0));
        assertEquals(1, aiServicePort.mainCallCount);
        assertTrue(aiServicePort.lastMainRequest.getContext().stream().anyMatch(entry -> entry.contains("Tuition")));
    }

    @Test
    void sendMessageShouldHandleGraduateOverviewWithRealDatabaseRetrieval() {
        ProgramFixture fixture = discoverProgramFixture();
        interpretationPort.nextInterpretation = overviewInterpretation(fixture);

        User user = userRepository.save(user("overview@example.com", "overview-user"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder()
                        .chatId(chat.getId())
                        .content("What do you know about " + fixture.universityMention() + "?")
                        .build()
        );

        assertEquals(MAIN_SUCCESS_CONTENT, result.getContent());
        assertFalse(result.getCitations().isEmpty());
        assertEquals(GraduateKnowledgeIntent.GRADUATE_OVERVIEW, retrievalPort.lastQuery.intent());
        assertEquals(1, retrievalPort.callCount);
        assertEquals(1, aiServicePort.mainCallCount);
        assertTrue(aiServicePort.lastMainRequest.getContext().stream().anyMatch(entry -> entry.contains("Programs:")));
        assertTrue(aiServicePort.lastMainRequest.getContext().stream().anyMatch(entry -> entry.contains("Tuition aggregation:")));
        assertEquals(1, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());
    }

    @Test
    void sendMessageShouldBypassGraduateRetrievalForGeneralChat() {
        interpretationPort.nextInterpretation = generalChatInterpretation();

        User user = userRepository.save(user("general@example.com", "general-user"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder()
                        .chatId(chat.getId())
                        .content("Tell me a joke")
                        .build()
        );

        assertEquals(MAIN_SUCCESS_CONTENT, result.getContent());
        assertTrue(result.getCitations().isEmpty());
        assertEquals(0, retrievalPort.callCount);
        assertEquals(1, aiServicePort.mainCallCount);
        assertTrue(aiServicePort.lastMainRequest.getContext().isEmpty());
        assertFalse(aiServicePort.lastMainRequest.getSystemPrompt().contains("Citation instructions:"));
        assertEquals(2, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).size());
    }

    @Test
    void sendMessageShouldClarifyAmbiguousUniversityWithoutRetrievalOrMainCall() {
        interpretationPort.nextInterpretation = ambiguousInterpretation();

        User user = userRepository.save(user("ambiguous@example.com", "ambiguous-user"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder()
                        .chatId(chat.getId())
                        .content("Tell me about the university")
                        .build()
        );

        assertTrue(result.getContent().contains("clearer university"), result.getContent());
        assertEquals(0, retrievalPort.callCount);
        assertEquals(0, aiServicePort.mainCallCount);
        assertEquals(2, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).size());
    }

    @Test
    void sendMessageShouldFallBackWhenInterpretationFails() {
        ProgramFixture fixture = discoverProgramFixture();
        interpretationPort.nextFailure = new IllegalStateException("interpretation unavailable");

        User user = userRepository.save(user("fallback@example.com", "fallback-user"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder()
                        .chatId(chat.getId())
                        .content("Tell me about " + fixture.universityName())
                        .build()
        );

        assertEquals(MAIN_SUCCESS_CONTENT, result.getContent());
        assertEquals(1, retrievalPort.callCount);
        assertEquals(1, aiServicePort.mainCallCount);
        assertEquals(1, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());
    }

    @Test
    void sendMessageShouldReturnGracefulFallbackWhenMainProviderFallsBack() {
        ProgramFixture fixture = discoverProgramFixture();
        interpretationPort.nextInterpretation = programInterpretation(fixture, false);
        aiServicePort.mainResponse = AiResponse.builder()
                .content(MAIN_FALLBACK_CONTENT)
                .provider("gemini")
                .model("gemini-2.5-flash")
                .fallback(true)
                .failureCategory(AiProviderFailureCategory.UNAVAILABLE)
                .retryable(true)
                .build();

        User user = userRepository.save(user("provider-fallback@example.com", "provider-fallback-user"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder()
                        .chatId(chat.getId())
                        .content("Tell me about " + fixture.universityAcronym())
                        .build()
        );

        assertEquals(MAIN_FALLBACK_CONTENT, result.getContent());
        assertTrue(result.getCitations().isEmpty());
        assertEquals(1, aiServicePort.mainCallCount);
        assertEquals(1, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());
    }

    @Test
    void sendMessageShouldRejectWhenBudgetCannotFitRequest() {
        ProgramFixture fixture = discoverProgramFixture();
        interpretationPort.nextInterpretation = programInterpretation(fixture, false);

        ChatApplicationService budgetLimitedService = serviceWithBudget(
                budgetManager("gemini", 40, 20, 20, 20)
        );

        User user = userRepository.save(user("budget@example.com", "budget-user"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        MessageResponseDto result = budgetLimitedService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder()
                        .chatId(chat.getId())
                        .content("Tell me about " + fixture.universityName())
                        .build()
        );

        assertEquals("AI service error : this message is from ChatApplicationService. Please try again later.", result.getContent());
        assertTrue(result.getCitations().isEmpty());
        assertEquals(0, aiServicePort.mainCallCount);
    }

    @Test
    void sendMessageShouldFilterCitationsAfterBudgetTrimming() {
        ProgramFixture fixture = discoverProgramFixture();
        interpretationPort.nextInterpretation = programInterpretation(fixture, false);
        retrievalPort.nextResult = new GraduateKnowledgeRetrievalResult(
                String.join("\n",
                        longContextEntry("S1", fixture, "kept"),
                        longContextEntry("S2", fixture, "trimmed"),
                        longContextEntry("S3", fixture, "trimmed"))
                        + "\n" + repeatText("extra context to trigger trimming ", 12),
                List.of(
                        citation("S1", fixture, fixture.officialProgramName(), 101L),
                        citation("S2", fixture, fixture.officialProgramName() + " advanced", 102L),
                        citation("S3", fixture, fixture.officialProgramName() + " extended", 103L)
                )
        );
        aiServicePort.mainResponse = AiResponse.builder()
                .content("Please review [S1] and [S3].")
                .provider("gemini")
                .model("gemini-2.5-flash")
                .build();

        ChatApplicationService trimmingService = serviceWithBudget(
                budgetManager("gemini", 1200, 120, 120, 60)
        );

        User user = userRepository.save(user("trim@example.com", "trim-user"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        MessageResponseDto result = trimmingService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder()
                        .chatId(chat.getId())
                        .content("Tell me about " + fixture.universityMention())
                        .build()
        );

        assertEquals(1, aiServicePort.mainCallCount);
        assertTrue(aiServicePort.lastMainRequest.getContext().stream().anyMatch(entry -> entry.contains("[S1]")));
        assertTrue(aiServicePort.lastMainRequest.getContext().stream().noneMatch(entry -> entry.contains("[S2]")));
        assertTrue(aiServicePort.lastMainRequest.getContext().stream().noneMatch(entry -> entry.contains("[S3]")));
        assertEquals(1, result.getCitations().size());
        assertEquals("S1", result.getCitations().get(0).label());
        assertEquals(1, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());
    }

    @Test
    void sendMessageShouldKeepAuxiliaryFailuresOutOfTheResponse() {
        ProgramFixture fixture = discoverProgramFixture();
        interpretationPort.nextInterpretation = programInterpretation(fixture, false);
        aiServicePort.memoryFailure = new IllegalStateException("memory update failed");
        aiServicePort.titleFailure = new IllegalStateException("title generation failed");

        User user = userRepository.save(user("aux@example.com", "aux-user"));
        Chat chat = chatRepository.save(Chat.builder().user(user).build());

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder()
                        .chatId(chat.getId())
                        .content("Tell me about " + fixture.universityName())
                        .build()
        );

        assertEquals(MAIN_SUCCESS_CONTENT, result.getContent());
        assertEquals(1, aiServicePort.mainCallCount);
        assertTrue(aiServicePort.memoryCallCount >= 1);
        assertTrue(aiServicePort.titleCallCount >= 1);
        assertEquals(1, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());
        assertNotNull(chatRepository.findById(chat.getId()).orElseThrow().getConversationMemory());
    }

    private ChatApplicationService serviceWithBudget(AiContextBudgetManager budgetManager) {
        return new ChatApplicationService(
                chatRepository,
                messageRepository,
                userRepository,
                aiServicePort,
                chatSystemPromptPort,
                interpretationPort,
                graduateQueryInterpreterPromptPort,
                graduateQueryInterpretationBudgetManager,
                graduateQueryInterpretationValidator,
                retrievalPort,
                universityCatalogRepository,
                graduateKnowledgeQueryInterpreter,
                graduateFollowUpResolver,
                budgetManager,
                conversationMemoryManager,
                chatTitleGenerationManager
        );
    }

    private GraduateQueryInterpretation programInterpretation(ProgramFixture fixture, boolean detailLevel) {
        return new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of(fixture.universityMention()),
                List.of(fixture.degreeTypeCode()),
                detailLevel ? "DETAILS" : "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );
    }

    private GraduateQueryInterpretation tuitionInterpretation(ProgramFixture fixture) {
        return new GraduateQueryInterpretation(
                1,
                "TUITION_AGGREGATION",
                List.of(fixture.universityMention()),
                List.of(fixture.degreeTypeCode()),
                null,
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );
    }

    private GraduateQueryInterpretation overviewInterpretation(ProgramFixture fixture) {
        return new GraduateQueryInterpretation(
                1,
                "GRADUATE_OVERVIEW",
                List.of(fixture.universityMention()),
                List.of(),
                null,
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );
    }

    private GraduateQueryInterpretation generalChatInterpretation() {
        return new GraduateQueryInterpretation(
                1,
                "GENERAL_CHAT",
                List.of(),
                List.of(),
                null,
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );
    }

    private GraduateQueryInterpretation ambiguousInterpretation() {
        return new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("Unknown University"),
                List.of(),
                "LIST",
                false,
                false,
                List.of(),
                true,
                null,
                List.of()
        );
    }

    private ProgramFixture discoverProgramFixture() {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT gp.id AS program_id,
                       gp.university_id,
                       u.name AS university_name,
                       u.acronym AS university_acronym,
                       gp.official_degree_name,
                       dt.code AS degree_type_code
                FROM graduate_program gp
                JOIN graduate_tuition_rate gtr ON gtr.program_id = gp.id AND gtr.amount IS NOT NULL
                JOIN university u ON u.id = gp.university_id
                JOIN degree_type dt ON dt.id = gp.degree_type_id
                WHERE gp.official_degree_name IS NOT NULL
                  AND dt.code IS NOT NULL
                ORDER BY gp.university_id, gp.id
                LIMIT 1
                """);

        Map<String, Object> tuitionRow = jdbcTemplate.queryForMap("""
                SELECT gtr.currency
                FROM graduate_tuition_rate gtr
                WHERE gtr.program_id = ?
                  AND gtr.amount IS NOT NULL
                ORDER BY gtr.id
                LIMIT 1
                """, row.get("program_id"));

        return new ProgramFixture(
                toLong(row.get("university_id")),
                stringValue(row.get("university_name")),
                stringValue(row.get("university_acronym")),
                stringValue(row.get("official_degree_name")),
                stringValue(row.get("degree_type_code")),
                stringValue(tuitionRow.get("currency"))
        );
    }

    private GraduateCitation citation(String label, ProgramFixture fixture, String programName, Long programId) {
        return new GraduateCitation(
                fixture.universityId() + "-" + programId,
                label,
                fixture.universityAcronym() + " " + programName,
                "https://example.com/" + label.toLowerCase(),
                "PROGRAM",
                fixture.universityId(),
                fixture.universityName(),
                programId,
                programName
        );
    }

    private String longContextEntry(String label, ProgramFixture fixture, String suffix) {
        return "[" + label + "] " + fixture.universityName() + " " + fixture.officialProgramName()
                + " " + suffix + " " + repeatText("lorem ipsum dolor sit amet ", 10);
    }

    private String repeatText(String text, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(text);
        }
        return builder.toString();
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? null : Long.valueOf(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private User user(String email, String username) {
        return User.builder()
                .email(email)
                .username(username)
                .password("Password123!")
                .isVerified(true)
                .build();
    }

    private AiContextBudgetManager budgetManager(
            String provider,
            int maxInputTokens,
            int reservedOutputTokens,
            int maxHistoryTokens,
            int maxRetrievalTokens
    ) {
        AiContextBudgetConfiguration configuration = new AiContextBudgetConfiguration(
                maxInputTokens,
                reservedOutputTokens,
                maxHistoryTokens,
                maxRetrievalTokens,
                4,
                0,
                Map.of(provider, new AiContextBudgetConfiguration.ProviderBudget(
                        maxInputTokens,
                        reservedOutputTokens,
                        maxHistoryTokens,
                        maxRetrievalTokens,
                        0
                ))
        );
        return new AiContextBudgetManager(configuration, new AiTokenEstimator(configuration), provider);
    }

    private record ProgramFixture(
            Long universityId,
            String universityName,
            String universityAcronym,
            String officialProgramName,
            String degreeTypeCode,
            String currency
    ) {
        String universityMention() {
            return universityAcronym != null && !universityAcronym.isBlank() ? universityAcronym : universityName;
        }
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        RoutingAiServicePort routingAiServicePort() {
            return new RoutingAiServicePort();
        }

        @Bean
        @Primary
        ControlledGraduateQueryInterpretationPort controlledGraduateQueryInterpretationPort() {
            return new ControlledGraduateQueryInterpretationPort();
        }

        @Bean
        @Primary
        Executor testChatTitleExecutor() {
            return Runnable::run;
        }

        @Bean
        @Primary
        CountingGraduateKnowledgeRetrievalPort countingGraduateKnowledgeRetrievalPort(SqlGraduateKnowledgeRetrievalAdapter delegate) {
            return new CountingGraduateKnowledgeRetrievalPort(delegate);
        }
    }

    static final class RoutingAiServicePort implements AiServicePort {
        private final Deque<AiRequest> mainRequests = new ArrayDeque<>();
        private final Deque<AiRequest> titleRequests = new ArrayDeque<>();
        private final Deque<AiRequest> memoryRequests = new ArrayDeque<>();

        private AiRequest lastMainRequest;
        private AiRequest lastTitleRequest;
        private AiRequest lastMemoryRequest;
        private int mainCallCount;
        private int titleCallCount;
        private int memoryCallCount;
        private RuntimeException mainFailure;
        private RuntimeException titleFailure;
        private RuntimeException memoryFailure;
        private AiResponse mainResponse = AiResponse.builder()
                .content(MAIN_SUCCESS_CONTENT)
                .provider("gemini")
                .model("gemini-2.5-flash")
                .fallback(false)
                .failureCategory(AiProviderFailureCategory.NONE)
                .retryable(false)
                .build();
        private AiResponse titleResponse = AiResponse.builder()
                .content(TITLE_CONTENT)
                .provider("gemini")
                .model("gemini-2.5-flash")
                .fallback(false)
                .failureCategory(AiProviderFailureCategory.NONE)
                .retryable(false)
                .build();
        private AiResponse memoryResponse = AiResponse.builder()
                .content(MEMORY_PATCH)
                .provider("gemini")
                .model("gemini-2.5-flash")
                .fallback(false)
                .failureCategory(AiProviderFailureCategory.NONE)
                .retryable(false)
                .build();

        @Override
        public AiResponse generateResponse(AiRequest request) {
            String systemPrompt = request != null ? request.getSystemPrompt() : null;
            if (systemPrompt != null && systemPrompt.contains("strict JSON patch generator")) {
                memoryCallCount++;
                lastMemoryRequest = request;
                memoryRequests.add(request);
                if (memoryFailure != null) {
                    throw memoryFailure;
                }
                return memoryResponse;
            }
            if (systemPrompt != null && systemPrompt.contains("Generate one concise chat title")) {
                titleCallCount++;
                lastTitleRequest = request;
                titleRequests.add(request);
                if (titleFailure != null) {
                    throw titleFailure;
                }
                return titleResponse;
            }

            mainCallCount++;
            lastMainRequest = request;
            mainRequests.add(request);
            if (mainFailure != null) {
                throw mainFailure;
            }
            return mainResponse;
        }

        private void reset() {
            mainRequests.clear();
            titleRequests.clear();
            memoryRequests.clear();
            lastMainRequest = null;
            lastTitleRequest = null;
            lastMemoryRequest = null;
            mainCallCount = 0;
            titleCallCount = 0;
            memoryCallCount = 0;
            mainFailure = null;
            titleFailure = null;
            memoryFailure = null;
            mainResponse = AiResponse.builder()
                    .content(MAIN_SUCCESS_CONTENT)
                    .provider("gemini")
                    .model("gemini-2.5-flash")
                    .fallback(false)
                    .failureCategory(AiProviderFailureCategory.NONE)
                    .retryable(false)
                    .build();
            titleResponse = AiResponse.builder()
                    .content(TITLE_CONTENT)
                    .provider("gemini")
                    .model("gemini-2.5-flash")
                    .fallback(false)
                    .failureCategory(AiProviderFailureCategory.NONE)
                    .retryable(false)
                    .build();
            memoryResponse = AiResponse.builder()
                    .content(MEMORY_PATCH)
                    .provider("gemini")
                    .model("gemini-2.5-flash")
                    .fallback(false)
                    .failureCategory(AiProviderFailureCategory.NONE)
                    .retryable(false)
                    .build();
        }
    }

    static final class ControlledGraduateQueryInterpretationPort implements GraduateQueryInterpretationPort {
        private GraduateQueryInterpretation nextInterpretation;
        private RuntimeException nextFailure;
        private int callCount;

        @Override
        public GraduateQueryInterpretation interpret(com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest request) {
            callCount++;
            if (nextFailure != null) {
                throw nextFailure;
            }
            return nextInterpretation;
        }

        private void reset() {
            nextInterpretation = null;
            nextFailure = null;
            callCount = 0;
        }
    }

    static final class CountingGraduateKnowledgeRetrievalPort implements GraduateKnowledgeRetrievalPort {
        private final SqlGraduateKnowledgeRetrievalAdapter delegate;
        private GraduateKnowledgeRetrievalResult nextResult;
        private int callCount;
        private GraduateKnowledgeQuery lastQuery;

        private CountingGraduateKnowledgeRetrievalPort(SqlGraduateKnowledgeRetrievalAdapter delegate) {
            this.delegate = delegate;
        }

        @Override
        public GraduateKnowledgeRetrievalResult retrieveContext(GraduateKnowledgeQuery query) {
            callCount++;
            lastQuery = query;
            if (nextResult != null) {
                return nextResult;
            }
            return delegate.retrieveContext(query);
        }

        private void reset() {
            nextResult = null;
            callCount = 0;
            lastQuery = null;
        }
    }
}
