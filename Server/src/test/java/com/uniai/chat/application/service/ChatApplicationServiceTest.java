package com.uniai.chat.application.service;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.budget.AiContextBudgetConfiguration;
import com.uniai.chat.application.budget.AiContextBudgetManager;
import com.uniai.chat.application.budget.AiTokenEstimator;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetConfiguration;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetManager;
import com.uniai.chat.application.citation.GraduateCitation;
import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.dto.command.SendMessageCommand;
import com.uniai.chat.application.dto.response.MessageResponseDto;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationMemoryManager;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretation;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationValidator;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.ChatSystemPromptPort;
import com.uniai.chat.application.port.out.GraduateQueryInterpretationPort;
import com.uniai.chat.application.port.out.GraduateQueryInterpreterPromptPort;
import com.uniai.chat.application.port.out.GraduateKnowledgeRetrievalPort;
import com.uniai.chat.application.retrieval.GraduateFollowUpResolutionResult;
import com.uniai.chat.application.retrieval.GraduateFollowUpResolver;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQueryInterpreter;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import com.uniai.chat.application.title.ChatTitleGenerationConfiguration;
import com.uniai.chat.application.title.ChatTitleGenerationManager;
import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.model.Message;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatApplicationServiceTest {

    private InMemoryChatRepository chatRepository;
    private InMemoryMessageRepository messageRepository;
    private InMemoryUserRepository userRepository;
    private InMemoryUniversityCatalogRepository universityCatalogRepository;
    private RecordingAiServicePort aiServicePort;
    private FixedChatSystemPromptPort chatSystemPromptPort;
    private FixedGraduateQueryInterpreterPromptPort graduateQueryInterpreterPromptPort;
    private RecordingGraduateQueryInterpretationPort graduateQueryInterpretationPort;
    private RecordingGraduateKnowledgeQueryInterpreter graduateKnowledgeQueryInterpreter;
    private RecordingGraduateFollowUpResolver graduateFollowUpResolver;
    private RecordingGraduateKnowledgeRetrievalPort graduateKnowledgeRetrievalPort;
    private RecordingConversationMemoryManager conversationMemoryManager;
    private RecordingChatTitleGenerationManager chatTitleGenerationManager;
    private SimpleMeterRegistry meterRegistry;
    private AiContextBudgetManager aiContextBudgetManager;
    private GraduateQueryInterpretationBudgetManager graduateQueryInterpretationBudgetManager;
    private GraduateQueryInterpretationValidator graduateQueryInterpretationValidator;
    private ChatApplicationService chatApplicationService;

    @BeforeEach
    void setUp() {
        chatRepository = new InMemoryChatRepository();
        messageRepository = new InMemoryMessageRepository();
        userRepository = new InMemoryUserRepository();
        universityCatalogRepository = new InMemoryUniversityCatalogRepository();
        aiServicePort = new RecordingAiServicePort();
        chatSystemPromptPort = new FixedChatSystemPromptPort("Static uniAI system prompt");
        graduateQueryInterpreterPromptPort = new FixedGraduateQueryInterpreterPromptPort("Interpretation prompt");
        graduateQueryInterpretationPort = new RecordingGraduateQueryInterpretationPort(
                new GraduateQueryInterpretation(
                        1,
                        "PROGRAM_LOOKUP",
                        List.of("USJ"),
                        List.of("MASTER"),
                        "LIST",
                        true,
                        false,
                        List.of(),
                        false,
                        null,
                        List.of()
                )
        );
        graduateKnowledgeQueryInterpreter = new RecordingGraduateKnowledgeQueryInterpreter();
        graduateKnowledgeRetrievalPort = new RecordingGraduateKnowledgeRetrievalPort(
                new GraduateKnowledgeRetrievalResult(
                        "Structured graduate context",
                        List.of(new GraduateCitation(
                                "program-1-10-1",
                                "S1",
                                "AUB Master of Science in Computer Science",
                                "https://www.aub.edu.lb/fas/cs/Pages/cmps_graduate.aspx",
                                "PROGRAM",
                                1L,
                                "American University of Beirut",
                                10L,
                                "Master of Science in Computer Science"
                        ))
                )
        );
        conversationMemoryManager = new RecordingConversationMemoryManager();
        chatTitleGenerationManager = new RecordingChatTitleGenerationManager();
        meterRegistry = new SimpleMeterRegistry();
        conversationMemoryManager.loadedMemory = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(new com.uniai.chat.application.memory.MemoryUniversityRef(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                "PROGRAM_LOOKUP",
                false,
                List.of(),
                List.of("tuition"),
                List.of(),
                List.of(),
                new com.uniai.chat.application.memory.ConversationPreferences("ENGLISH", null, null)
        );
        aiContextBudgetManager = budgetManager("gemini", 200000, 2000, 12000, 120000, 4, 128);
        graduateQueryInterpretationBudgetManager = interpretationBudgetManager("gemini", 1500, 250, 4, 0);
        graduateQueryInterpretationValidator = new GraduateQueryInterpretationValidator();
        graduateFollowUpResolver = new RecordingGraduateFollowUpResolver();

        chatApplicationService = new ChatApplicationService(
                chatRepository,
                messageRepository,
                userRepository,
                aiServicePort,
                chatSystemPromptPort,
                graduateQueryInterpretationPort,
                graduateQueryInterpreterPromptPort,
                graduateQueryInterpretationBudgetManager,
                graduateQueryInterpretationValidator,
                graduateKnowledgeRetrievalPort,
                universityCatalogRepository,
                graduateKnowledgeQueryInterpreter,
                graduateFollowUpResolver,
                aiContextBudgetManager,
                conversationMemoryManager,
                chatTitleGenerationManager,
                null,
                meterRegistry
        );
    }

    @Test
    void sendMessageShouldBuildAiRequestWithSystemPromptHistoryContextAndCurrentMessage() {
        User user = user(1L, "alice", "alice@example.com");
        Chat chat = chat(10L, user, "chat-title");
        userRepository.save(user);
        chatRepository.save(chat);
        seedChatMessages(chat, user, 42L, 7);
        SendMessageCommand command = SendMessageCommand.builder()
                .chatId(chat.getId())
                .content("What about USJ?")
                .build();

        MessageResponseDto result = chatApplicationService.sendMessage(user.getEmail(), command);

        assertTrue(aiServicePort.lastRequest.getSystemPrompt().contains("Static uniAI system prompt"));
        assertTrue(aiServicePort.lastRequest.getSystemPrompt().contains("Citation instructions:"));
        assertTrue(aiServicePort.lastRequest.getSystemPrompt().contains("Sources:"));
        assertTrue(aiServicePort.lastRequest.getSystemPrompt().contains("[S1] AUB Master of Science in Computer Science"));
        assertEquals("What about USJ?", aiServicePort.lastRequest.getUserMessage());
        assertEquals("Structured graduate context", aiServicePort.lastRequest.getContext().get(0));
        assertEquals(2000, aiServicePort.lastRequest.getMaxTokens());
        assertEquals(6, aiServicePort.lastRequest.getConversationHistory().size());
        assertFalse(aiServicePort.lastRequest.getConversationHistory().stream()
                .anyMatch(message -> "What about USJ?".equals(message.getContent())));
        assertEquals(List.of(
                "AUB master's answer",
                "What about tuition?",
                "AUB tuition answer",
                "And admission?",
                "AUB admission answer",
                "Any scholarships?"
        ), aiServicePort.lastRequest.getConversationHistory().stream().map(AiConversationMessage::getContent).toList());

        assertEquals(1, graduateQueryInterpretationPort.callCount);
        assertEquals("What about USJ?", graduateQueryInterpretationPort.lastRequest.userMessage());
        assertEquals(4, graduateQueryInterpretationPort.lastRequest.recentConversationHistory().size());
        assertEquals(conversationMemoryManager.loadedMemory, graduateQueryInterpretationPort.lastRequest.conversationMemory());
        assertEquals(1, conversationMemoryManager.loadCallCount);

        assertEquals(1, graduateKnowledgeRetrievalPort.callCount);
        assertEquals(GraduateKnowledgeIntent.PROGRAM_LOOKUP, graduateKnowledgeRetrievalPort.lastQuery.intent());
        assertEquals(1, graduateKnowledgeRetrievalPort.lastQuery.resolvedUniversities().size());
        assertEquals("USJ", graduateKnowledgeRetrievalPort.lastQuery.resolvedUniversities().get(0).acronym());
        assertEquals(1, universityCatalogRepository.findAllCount);
        assertEquals(1, graduateFollowUpResolver.callCount);

        assertEquals("Here is the official answer.", result.getContent());
        assertEquals(0, result.getCitations().size());
        assertEquals("Here is the official answer.", messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).get(messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).size() - 1).getContent());
        assertEquals(1, aiServicePort.callCount);
        assertEquals(conversationMemoryManager.loadedMemory, aiServicePort.lastRequest.getConversationMemory());
        assertEquals(1, conversationMemoryManager.updateCallCount);
        assertEquals(GraduateKnowledgeIntent.PROGRAM_LOOKUP, graduateKnowledgeRetrievalPort.lastQuery.intent());
        assertEquals(1, graduateKnowledgeRetrievalPort.lastQuery.resolvedUniversities().size());
        assertEquals("USJ", graduateKnowledgeRetrievalPort.lastQuery.resolvedUniversities().get(0).acronym());
        assertEquals(4, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());
        assertEquals("What about USJ?", aiServicePort.lastRequest.getUserMessage());
        assertEquals("Structured graduate context", aiServicePort.lastRequest.getContext().get(0));
        assertEquals(0, chatTitleGenerationManager.callCount);
        assertEquals(1L, meterRegistry.find("uniai.chat.request.duration")
                .tags("outcome", "success")
                .timer()
                .count());
    }

    @Test
    void sendMessageShouldScheduleTitleGenerationOnlyForFirstPersistedUserTurn() {
        User user = user(11L, "mona", "mona@example.com");
        Chat chat = chat(110L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);

        chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Compare AUB and LAU tuition").build()
        );

        assertEquals(1, chatTitleGenerationManager.callCount);
        assertEquals(chat.getId(), chatTitleGenerationManager.lastChatId);
        assertEquals("Compare AUB and LAU tuition", chatTitleGenerationManager.lastFirstUserMessage);
        assertFalse(chatTitleGenerationManager.transactionActiveAtCall);

        chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("And admissions?").build()
        );

        assertEquals(1, chatTitleGenerationManager.callCount);
    }

    @Test
    void sendMessageShouldNotRetryTitleGenerationAfterFirstAttemptFailure() {
        User user = user(12L, "nora", "nora@example.com");
        Chat chat = chat(120L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        chatTitleGenerationManager.failNextCall = true;

        MessageResponseDto firstResult = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Compare AUB and LAU tuition").build()
        );

        assertNotNull(firstResult.getContent());
        assertEquals(1, chatTitleGenerationManager.callCount);

        chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("And admissions?").build()
        );

        assertEquals(1, chatTitleGenerationManager.callCount);
    }

    @Test
    void sendMessageShouldSaveSafeFallbackContentWhenAiResponseIsFallbackWithoutContent() {
        User user = user(2L, "bob", "bob@example.com");
        Chat chat = chat(20L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        aiServicePort.nextResponse = AiResponse.builder()
                .fallback(true)
                .content(null)
                .provider("gemini")
                .model("gemini-2.5-flash")
                .build();

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Hello").build()
        );

        assertEquals("AI service error : this message is from ChatApplicationService. Please try again later.", result.getContent());
        List<Message> messages = messageRepository.findByChatIdOrderByTimestampAsc(chat.getId());
        assertEquals(2, messages.size());
        assertEquals("AI service error : this message is from ChatApplicationService. Please try again later.", messages.get(1).getContent());
        assertTrue(aiServicePort.lastRequest.getConversationHistory().isEmpty());
        assertTrue(aiServicePort.lastRequest.getSystemPrompt().contains("Static uniAI system prompt"));
    }

    @Test
    void sendMessageShouldReturnOnlyValidatedCitationLabelsFromAssistantResponse() {
        User user = user(22L, "sara", "sara@example.com");
        Chat chat = chat(220L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        aiServicePort.nextResponse = AiResponse.builder()
                .content("Please review [S1], [S1], [S99], and https://example.com.")
                .provider("gemini")
                .model("gemini-2.5-flash")
                .build();

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Tell me about AUB CS").build()
        );

        assertEquals(1, result.getCitations().size());
        assertEquals("S1", result.getCitations().get(0).label());
        assertEquals("AUB Master of Science in Computer Science", result.getCitations().get(0).title());
        assertEquals("https://www.aub.edu.lb/fas/cs/Pages/cmps_graduate.aspx", result.getCitations().get(0).url());
        assertEquals(1, result.getCitations().stream().filter(citation -> "S1".equals(citation.label())).count());
    }

    @Test
    void sendMessageShouldFilterCitationsToTheFinalBudgetedContext() {
        User user = user(23L, "tariq", "tariq@example.com");
        Chat chat = chat(230L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);

        RecordingAiServicePort budgetAwareAiServicePort = new RecordingAiServicePort();
        budgetAwareAiServicePort.nextResponse = AiResponse.builder()
                .content("Please review [S1] and [S3].")
                .provider("gemini")
                .model("gemini-2.5-flash")
                .build();

        RecordingGraduateKnowledgeRetrievalPort budgetAwareRetrievalPort = new RecordingGraduateKnowledgeRetrievalPort(
                new GraduateKnowledgeRetrievalResult(
                        """
                                AUB Master of Science in Computer Science
                                Query interpretation:
                                Intent: PROGRAM_LOOKUP
                                Resolved universities: American University of Beirut (AUB)
                                Degree types: MASTER

                                Programs:
                                1.
                                  Program name: Master of Science in Computer Science
                                  Official source URL(s): https://www.aub.edu.lb/fas/cs/Pages/cmps_graduate.aspx
                                  Official program URL: https://www.aub.edu.lb/fas/cs/Pages/cmps_graduate.aspx
                                  Extra context for the first program that should survive trimming.
                                  Extra context for the first program that should survive trimming.
                                  Extra context for the first program that should survive trimming.
                                2.
                                  Program name: Master of Science in Artificial Intelligence
                                  Official source URL(s): https://www.aub.edu.lb/fas/ai/Pages/msai.aspx
                                  Official program URL: https://www.aub.edu.lb/fas/ai/Pages/msai.aspx
                                  Extra context for the second program that should be removed by trimming.
                                  Extra context for the second program that should be removed by trimming.
                                  Extra context for the second program that should be removed by trimming.
                                3.
                                  Program name: Master of Science in Data Science
                                  Official source URL(s): https://www.aub.edu.lb/fas/ds/Pages/msds.aspx
                                  Official program URL: https://www.aub.edu.lb/fas/ds/Pages/msds.aspx
                                  Extra context for the third program that should be removed by trimming.
                                """,
                        List.of(
                                new GraduateCitation(
                                        "program-1-10-1",
                                        "S1",
                                        "AUB Master of Science in Computer Science",
                                        "https://www.aub.edu.lb/fas/cs/Pages/cmps_graduate.aspx",
                                        "PROGRAM",
                                        1L,
                                        "American University of Beirut",
                                        10L,
                                        "Master of Science in Computer Science"
                                ),
                                new GraduateCitation(
                                        "program-1-11-2",
                                        "S2",
                                        "AUB Master of Science in Artificial Intelligence",
                                        "https://www.aub.edu.lb/fas/ai/Pages/msai.aspx",
                                        "PROGRAM",
                                        1L,
                                        "American University of Beirut",
                                        11L,
                                        "Master of Science in Artificial Intelligence"
                                ),
                                new GraduateCitation(
                                        "program-1-12-3",
                                        "S3",
                                        "AUB Master of Science in Data Science",
                                        "https://www.aub.edu.lb/fas/ds/Pages/msds.aspx",
                                        "PROGRAM",
                                        1L,
                                        "American University of Beirut",
                                        12L,
                                        "Master of Science in Data Science"
                                )
                        )
                )
        );

        AiContextBudgetManager trimmingBudgetManager = budgetManager("gemini", 260, 60, 40, 28, 4, 0);
        ChatApplicationService trimmingService = new ChatApplicationService(
                chatRepository,
                messageRepository,
                userRepository,
                budgetAwareAiServicePort,
                chatSystemPromptPort,
                graduateQueryInterpretationPort,
                graduateQueryInterpreterPromptPort,
                graduateQueryInterpretationBudgetManager,
                graduateQueryInterpretationValidator,
                budgetAwareRetrievalPort,
                universityCatalogRepository,
                graduateKnowledgeQueryInterpreter,
                graduateFollowUpResolver,
                trimmingBudgetManager,
                conversationMemoryManager,
                chatTitleGenerationManager,
                null,
                meterRegistry
        );

        MessageResponseDto result = trimmingService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Tell me about AUB graduate programs").build()
        );

        assertEquals(1, budgetAwareAiServicePort.callCount);
        assertNotNull(budgetAwareAiServicePort.lastRequest);
        assertTrue(budgetAwareAiServicePort.lastRequest.getContext().stream()
                .anyMatch(context -> context.contains("Master of Science in Computer Science")));
        assertFalse(budgetAwareAiServicePort.lastRequest.getContext().stream()
                .anyMatch(context -> context.contains("Master of Science in Artificial Intelligence")));
        assertTrue(budgetAwareAiServicePort.lastRequest.getSystemPrompt().contains("[S1]"));
        assertFalse(budgetAwareAiServicePort.lastRequest.getSystemPrompt().contains("[S2]"));
        assertFalse(budgetAwareAiServicePort.lastRequest.getSystemPrompt().contains("[S3]"));
        assertEquals(1, result.getCitations().size());
        assertEquals("S1", result.getCitations().get(0).label());
        assertEquals(1, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());
    }

    @Test
    void sendMessageShouldKeepHistoryLocalToTheRequestedChat() {
        User user = user(3L, "carol", "carol@example.com");
        Chat primaryChat = chat(30L, user, null);
        Chat otherChat = chat(31L, user, null);
        userRepository.save(user);
        chatRepository.save(primaryChat);
        chatRepository.save(otherChat);

        seedChatMessages(primaryChat, user, 100L, 3);
        seedOtherChatMessages(otherChat, user);

        chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(primaryChat.getId()).content("Tell me more").build()
        );

        assertFalse(aiServicePort.lastRequest.getConversationHistory().stream()
                .anyMatch(message -> message.getContent().contains("Other chat")));
        assertFalse(graduateQueryInterpretationPort.lastRequest.recentConversationHistory().stream()
                .anyMatch(message -> message.getContent().contains("Other chat")));
    }

    @Test
    void sendMessageShouldSkipProviderWhenBudgetCannotFitRequiredContent() {
        AiContextBudgetManager smallBudgetManager = budgetManager("gemini", 12, 10, 10, 10, 4, 0);
        ChatApplicationService budgetLimitedService = new ChatApplicationService(
                chatRepository,
                messageRepository,
                userRepository,
                aiServicePort,
                chatSystemPromptPort,
                graduateQueryInterpretationPort,
                graduateQueryInterpreterPromptPort,
                graduateQueryInterpretationBudgetManager,
                graduateQueryInterpretationValidator,
                graduateKnowledgeRetrievalPort,
                universityCatalogRepository,
                graduateKnowledgeQueryInterpreter,
                graduateFollowUpResolver,
                smallBudgetManager,
                conversationMemoryManager,
                chatTitleGenerationManager,
                null,
                meterRegistry
        );

        User user = user(4L, "dan", "dan@example.com");
        Chat chat = chat(40L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);

        MessageResponseDto result = budgetLimitedService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Hello").build()
        );

        assertEquals(0, aiServicePort.callCount);
        assertEquals("AI service error : this message is from ChatApplicationService. Please try again later.", result.getContent());
        assertEquals(0, result.getCitations().size());
        List<Message> messages = messageRepository.findByChatIdOrderByTimestampAsc(chat.getId());
        assertEquals(2, messages.size());
        assertEquals("AI service error : this message is from ChatApplicationService. Please try again later.", messages.get(1).getContent());
        assertEquals(1, messages.stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());
        assertEquals(1, graduateQueryInterpretationPort.callCount);
        assertEquals(1L, meterRegistry.find("uniai.ai.budget.rejections")
                .tags("operation", "main_response", "provider", "gemini")
                .counter()
                .count());
    }

    @Test
    void sendMessageShouldHandleProviderFailuresSeparatelyFromBudgetRejection() {
        User user = user(5L, "erin", "erin@example.com");
        Chat chat = chat(50L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        aiServicePort.nextRuntimeException = new IllegalStateException("provider unavailable");

        assertThrows(IllegalStateException.class, () -> chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Hello").build()
        ));

        assertEquals(1, aiServicePort.callCount);
        assertEquals(1, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).size());
        assertEquals(1, graduateQueryInterpretationPort.callCount);
    }

    @Test
    void sendMessageShouldUseDeterministicFallbackWhenInterpretationFails() {
        User user = user(6L, "frank", "frank@example.com");
        Chat chat = chat(60L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        graduateQueryInterpretationPort.nextRuntimeException = new IllegalStateException("interpretation unavailable");

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("What master's programs does AUB offer?").build()
        );

        assertEquals("Here is the official answer.", result.getContent());
        assertEquals(1, aiServicePort.callCount);
        assertEquals(1, graduateKnowledgeRetrievalPort.callCount);
        assertEquals(1, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());
    }

    @Test
    void sendMessageShouldSkipRetrievalForUnsupportedDegreeRequestsWhenInterpretationFails() {
        User user = user(7L, "grace", "grace@example.com");
        Chat chat = chat(70L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        graduateQueryInterpretationPort.nextRuntimeException = new IllegalStateException("interpretation unavailable");

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Give me bachlour degrees for AUB").build()
        );

        assertEquals("I can help with master's and PhD graduate questions only.", result.getContent());
        assertEquals(0, graduateKnowledgeRetrievalPort.callCount);
        assertEquals(0, aiServicePort.callCount);
        assertEquals(2, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).size());
    }

    @Test
    void sendMessageShouldClarifyUnresolvedComparisonPronounBeforeRetrieval() {
        User user = user(8L, "hannah", "hannah@example.com");
        Chat chat = chat(80L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        conversationMemoryManager.loadedMemory = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(
                        new com.uniai.chat.application.memory.MemoryUniversityRef(1L, "American University of Beirut", "AUB"),
                        new com.uniai.chat.application.memory.MemoryUniversityRef(2L, "Université Saint-Joseph", "USJ")
                ),
                List.of("MASTER"),
                "PROGRAM_LOOKUP",
                true,
                List.of(
                        new com.uniai.chat.application.memory.MemoryUniversityRef(1L, "American University of Beirut", "AUB"),
                        new com.uniai.chat.application.memory.MemoryUniversityRef(2L, "Université Saint-Joseph", "USJ")
                ),
                List.of(),
                List.of(),
                List.of(),
                new com.uniai.chat.application.memory.ConversationPreferences("ENGLISH", null, null)
        );
        graduateQueryInterpretationPort.nextInterpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of(),
                List.of(),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("it").build()
        );

        assertEquals("Do you mean the first or second university?", result.getContent());
        assertEquals(0, graduateKnowledgeRetrievalPort.callCount);
        assertEquals(0, aiServicePort.callCount);
        assertEquals(2, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).size());
        assertEquals(1, graduateFollowUpResolver.callCount);
        assertEquals(0.0d, counterCount(
                "uniai.ai.interpretation.invalid",
                "provider", "unknown",
                "model", "unknown",
                "reason", "malformed_json"
        ));
    }

    @Test
    void sendMessageShouldClarifyUnknownUniversityBeforeRetrieval() {
        User user = user(9L, "ivan", "ivan@example.com");
        Chat chat = chat(90L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        conversationMemoryManager.loadedMemory = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(new com.uniai.chat.application.memory.MemoryUniversityRef(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                "PROGRAM_LOOKUP",
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new com.uniai.chat.application.memory.ConversationPreferences("ENGLISH", null, null)
        );
        graduateQueryInterpretationPort.nextInterpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("AUB"),
                List.of("MASTER"),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("What about XYZ University?").build()
        );

        assertEquals("Which university are you referring to?", result.getContent());
        assertEquals(0, graduateKnowledgeRetrievalPort.callCount);
        assertEquals(0, aiServicePort.callCount);
        assertEquals(2, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).size());
        assertEquals(1, graduateFollowUpResolver.callCount);
    }

    @Test
    void sendMessageShouldClarifyUnknownDegreeBeforeRetrieval() {
        User user = user(10L, "julia", "julia@example.com");
        Chat chat = chat(100L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        conversationMemoryManager.loadedMemory = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(new com.uniai.chat.application.memory.MemoryUniversityRef(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                "PROGRAM_LOOKUP",
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new com.uniai.chat.application.memory.ConversationPreferences("ENGLISH", null, null)
        );
        graduateQueryInterpretationPort.nextInterpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("AUB"),
                List.of("MASTER"),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("What about the graduate diploma?").build()
        );

        assertEquals("Which degree type should I use?", result.getContent());
        assertEquals(0, graduateKnowledgeRetrievalPort.callCount);
        assertEquals(0, aiServicePort.callCount);
        assertEquals(2, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).size());
        assertEquals(1, graduateFollowUpResolver.callCount);
    }

    @Test
    void sendMessageShouldRecordInvalidInterpretationMetricsWithoutTreatingAmbiguityAsInvalid() {
        User user = user(13L, "lina", "lina@example.com");
        Chat chat = chat(130L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        graduateQueryInterpretationPort.nextInterpretation = new GraduateQueryInterpretation(
                1,
                null,
                List.of(),
                List.of(),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Tell me about AUB graduate programs").build()
        );

        assertEquals("Here is the official answer.", result.getContent());
        assertEquals(1, aiServicePort.callCount);
        assertEquals(1, graduateKnowledgeRetrievalPort.callCount);
        assertEquals(1.0d, counterCount(
                "uniai.ai.interpretation.invalid",
                "provider", "unknown",
                "model", "unknown",
                "reason", "AI_QUERY_INTERPRETATION_INVALID_INTENT"
        ));
        assertEquals(0.0d, counterCount(
                "uniai.ai.interpretation.invalid",
                "provider", "unknown",
                "model", "unknown",
                "reason", "malformed_json"
        ));
    }

    @Test
    void sendMessageShouldIgnoreAssistantHistoryWhenResolvingFollowUp() {
        User user = user(11L, "kate", "kate@example.com");
        Chat chat = chat(110L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        messageRepository.save(Message.builder()
                .chat(chat)
                .senderId(user.getId())
                .content("What master's programs does AUB offer?")
                .timestamp(LocalDateTime.now().minusMinutes(5))
                .build());
        messageRepository.save(Message.builder()
                .chat(chat)
                .senderId(0L)
                .content("LAU also offers several PhD programs.")
                .timestamp(LocalDateTime.now().minusMinutes(4))
                .build());
        graduateQueryInterpretationPort.nextInterpretation = new GraduateQueryInterpretation(
                1,
                "TUITION_AGGREGATION",
                List.of(),
                List.of(),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("How much does it cost?").build()
        );

        assertEquals("Here is the official answer.", result.getContent());
        assertEquals(1, graduateKnowledgeRetrievalPort.callCount);
        assertEquals("AUB", graduateKnowledgeRetrievalPort.lastQuery.resolvedUniversities().get(0).acronym());
        assertEquals(List.of("MASTER"), graduateKnowledgeRetrievalPort.lastQuery.degreeTypes());
        assertEquals(1, aiServicePort.callCount);
        assertEquals(1, graduateFollowUpResolver.callCount);
    }

    private void seedChatMessages(Chat chat, User user, long baseMinutesAgo, int existingMessageCount) {
        List<String> contents = List.of(
                "What master's programs does AUB offer?",
                "AUB master's answer",
                "What about tuition?",
                "AUB tuition answer",
                "And admission?",
                "AUB admission answer",
                "Any scholarships?"
        );

        for (int i = 0; i < existingMessageCount; i++) {
            Message message = Message.builder()
                    .chat(chat)
                    .senderId(i % 2 == 0 ? user.getId() : 0L)
                    .content(contents.get(i))
                    .timestamp(LocalDateTime.now().minusMinutes(baseMinutesAgo - i))
                    .build();
            messageRepository.save(message);
        }
    }

    private void seedOtherChatMessages(Chat otherChat, User user) {
        messageRepository.save(Message.builder()
                .chat(otherChat)
                .senderId(user.getId())
                .content("Other chat message")
                .timestamp(LocalDateTime.now().minusMinutes(1))
                .build());
    }

    private User user(Long id, String username, String email) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password("Password123!")
                .build();
    }

    private AiContextBudgetManager budgetManager(
            String provider,
            int maxInputTokens,
            int reservedOutputTokens,
            int maxHistoryTokens,
            int maxRetrievalTokens,
            int charactersPerToken,
            int requestOverheadTokens
    ) {
        return budgetManager(provider, maxInputTokens, reservedOutputTokens, maxHistoryTokens, maxRetrievalTokens, charactersPerToken, requestOverheadTokens, meterRegistry);
    }

    private AiContextBudgetManager budgetManager(
            String provider,
            int maxInputTokens,
            int reservedOutputTokens,
            int maxHistoryTokens,
            int maxRetrievalTokens,
            int charactersPerToken,
            int requestOverheadTokens,
            SimpleMeterRegistry metrics
    ) {
        AiContextBudgetConfiguration configuration = new AiContextBudgetConfiguration(
                maxInputTokens,
                reservedOutputTokens,
                maxHistoryTokens,
                maxRetrievalTokens,
                charactersPerToken,
                requestOverheadTokens,
                Map.of(provider, new AiContextBudgetConfiguration.ProviderBudget(
                        maxInputTokens,
                        reservedOutputTokens,
                        maxHistoryTokens,
                        maxRetrievalTokens,
                        requestOverheadTokens
                ))
        );
        return new AiContextBudgetManager(configuration, new AiTokenEstimator(configuration), provider, metrics);
    }

    private GraduateQueryInterpretationBudgetManager interpretationBudgetManager(
            String provider,
            long maxInputTokens,
            int maxOutputTokens,
            int historyMessageLimit,
            int requestOverheadTokens
    ) {
        GraduateQueryInterpretationBudgetConfiguration configuration = new GraduateQueryInterpretationBudgetConfiguration(
                true,
                maxInputTokens,
                maxOutputTokens,
                historyMessageLimit,
                "prompts/graduate-query-interpreter-prompt.txt"
        );
        AiContextBudgetConfiguration estimatorConfiguration = new AiContextBudgetConfiguration(
                200000,
                2000,
                12000,
                120000,
                4,
                requestOverheadTokens,
                Map.of(provider, new AiContextBudgetConfiguration.ProviderBudget(
                        200000,
                        2000,
                        12000,
                        120000,
                        requestOverheadTokens
                ))
        );
        return new GraduateQueryInterpretationBudgetManager(configuration, new AiTokenEstimator(estimatorConfiguration), provider);
    }

    private static final class RecordingGraduateKnowledgeQueryInterpreter extends GraduateKnowledgeQueryInterpreter {
        private String lastUserMessage;
        private List<AiConversationMessage> lastRecentConversationWindow = Collections.emptyList();
        private List<UniversityCatalog> lastUniversityCatalogs = Collections.emptyList();
        private ConversationMemory lastConversationMemory = ConversationMemory.empty();
        private GraduateKnowledgeQuery lastQuery = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS,
                List.of(),
                List.of(),
                null,
                false,
                true
        );

        @Override
        public GraduateKnowledgeQuery interpret(String userMessage, List<AiConversationMessage> recentConversationHistory, List<UniversityCatalog> universityCatalogs) {
            return interpret(userMessage, recentConversationHistory, universityCatalogs, ConversationMemory.empty());
        }

        @Override
        public GraduateKnowledgeQuery interpret(String userMessage, List<AiConversationMessage> recentConversationHistory, List<UniversityCatalog> universityCatalogs, ConversationMemory conversationMemory) {
            lastUserMessage = userMessage;
            lastRecentConversationWindow = recentConversationHistory == null
                    ? Collections.emptyList()
                    : List.copyOf(recentConversationHistory);
            lastUniversityCatalogs = universityCatalogs == null
                    ? Collections.emptyList()
                    : List.copyOf(universityCatalogs);
            lastConversationMemory = conversationMemory == null ? ConversationMemory.empty() : conversationMemory;
            lastQuery = new GraduateKnowledgeQuery(
                    GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                    List.of(new ResolvedUniversity(2L, "Université Saint-Joseph", "USJ")),
                    List.of("MASTER"),
                    GraduateProgramDetailLevel.LIST,
                    true,
                    false
            );
            return lastQuery;
        }
    }

    private static final class RecordingConversationMemoryManager extends ConversationMemoryManager {
        private ConversationMemory loadedMemory = ConversationMemory.empty();
        private int loadCallCount;
        private int updateCallCount;

        private RecordingConversationMemoryManager() {
            super(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    new com.uniai.chat.application.budget.ConversationMemoryBudgetConfiguration(false, 1, 1, "prompts/conversation-memory-updater-prompt.txt")
            );
        }

        @Override
        public ConversationMemory loadMemory(Long chatId) {
            loadCallCount++;
            return loadedMemory;
        }

        @Override
        public void updateMemoryIfNeeded(Long chatId, ConversationMemory previousMemory, String currentUserMessage, String assistantResponse, com.uniai.chat.application.interpretation.GraduateQueryInterpretationResult interpretationResult) {
            updateCallCount++;
        }
    }

    private static final class RecordingChatTitleGenerationManager extends ChatTitleGenerationManager {
        private int callCount;
        private Long lastChatId;
        private String lastFirstUserMessage;
        private boolean transactionActiveAtCall;
        private boolean failNextCall;

        private RecordingChatTitleGenerationManager() {
            super(
                    null,
                    null,
                    null,
                    null,
                    new ChatTitleGenerationConfiguration(false, 1, 1, 60),
                    Runnable::run
            );
        }

        @Override
        public void generateTitleIfNeeded(Long chatId, String firstUserMessage) {
            callCount++;
            lastChatId = chatId;
            lastFirstUserMessage = firstUserMessage;
            transactionActiveAtCall = TransactionSynchronizationManager.isActualTransactionActive();
            if (failNextCall) {
                failNextCall = false;
                throw new IllegalStateException("title generation failed");
            }
        }
    }

    private static final class RecordingGraduateQueryInterpretationPort implements GraduateQueryInterpretationPort {
        private GraduateQueryInterpretation nextInterpretation;
        private RuntimeException nextRuntimeException;
        private int callCount;
        private GraduateQueryInterpretationRequest lastRequest;

        private RecordingGraduateQueryInterpretationPort(GraduateQueryInterpretation nextInterpretation) {
            this.nextInterpretation = nextInterpretation;
        }

        @Override
        public GraduateQueryInterpretation interpret(GraduateQueryInterpretationRequest request) {
            callCount++;
            lastRequest = request;
            if (nextRuntimeException != null) {
                throw nextRuntimeException;
            }
            return nextInterpretation;
        }
    }

    private static final class RecordingGraduateFollowUpResolver extends GraduateFollowUpResolver {
        private int callCount;
        private String lastCurrentMessage;
        private GraduateKnowledgeQuery lastCandidateQuery;
        private GraduateFollowUpResolutionResult lastResult;

        @Override
        public GraduateFollowUpResolutionResult resolve(
                String currentUserMessage,
                GraduateKnowledgeQuery candidateQuery,
                List<AiConversationMessage> recentConversationHistory,
                ConversationMemory conversationMemory,
                List<UniversityCatalog> universityCatalogs
        ) {
            callCount++;
            lastCurrentMessage = currentUserMessage;
            lastCandidateQuery = candidateQuery;
            lastResult = super.resolve(currentUserMessage, candidateQuery, recentConversationHistory, conversationMemory, universityCatalogs);
            return lastResult;
        }
    }

    private static final class InMemoryUniversityCatalogRepository implements UniversityCatalogRepository {
        private int findAllCount;

        @Override
        public List<UniversityCatalog> findAll() {
            findAllCount++;
            return List.of(
                    UniversityCatalog.builder().id(1L).name("American University of Beirut").acronym("AUB").build(),
                    UniversityCatalog.builder().id(2L).name("Université Saint-Joseph").acronym("USJ").build(),
                    UniversityCatalog.builder().id(3L).name("Lebanese National Conservatory").acronym("LNC").build()
            );
        }

        @Override
        public List<UniversityCatalog> searchByName(String search) {
            return findAll().stream().filter(university -> search != null && (university.getName().contains(search) || (university.getAcronym() != null && university.getAcronym().contains(search)))).toList();
        }
    }

    private Chat chat(Long id, User user, String title) {
        return Chat.builder()
                .id(id)
                .user(user)
                .title(title)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusMinutes(1))
                .build();
    }

    private static final class RecordingAiServicePort implements AiServicePort {
        private AiRequest lastRequest;
        private int callCount;
        private RuntimeException nextRuntimeException;
        private AiResponse nextResponse = AiResponse.builder()
                .content("Here is the official answer.")
                .provider("gemini")
                .model("gemini-2.5-flash")
                .build();

        @Override
        public AiResponse generateResponse(AiRequest request) {
            callCount++;
            lastRequest = request;
            if (nextRuntimeException != null) {
                throw nextRuntimeException;
            }
            return nextResponse;
        }
    }

    private double counterCount(String metric, String... tags) {
        Counter counter = meterRegistry.find(metric).tags(tags).counter();
        return counter == null ? 0.0d : counter.count();
    }

    private static final class FixedChatSystemPromptPort implements ChatSystemPromptPort {
        private final String prompt;

        private FixedChatSystemPromptPort(String prompt) {
            this.prompt = prompt;
        }

        @Override
        public String getPrompt() {
            return prompt;
        }
    }

    private static final class FixedGraduateQueryInterpreterPromptPort implements GraduateQueryInterpreterPromptPort {
        private final String prompt;

        private FixedGraduateQueryInterpreterPromptPort(String prompt) {
            this.prompt = prompt;
        }

        @Override
        public String getPrompt() {
            return prompt;
        }
    }

    private static final class RecordingGraduateKnowledgeRetrievalPort implements GraduateKnowledgeRetrievalPort {
        private final GraduateKnowledgeRetrievalResult result;
        private int callCount;
        private GraduateKnowledgeQuery lastQuery;

        private RecordingGraduateKnowledgeRetrievalPort(GraduateKnowledgeRetrievalResult result) {
            this.result = result;
        }

        @Override
        public GraduateKnowledgeRetrievalResult retrieveContext(GraduateKnowledgeQuery query) {
            callCount++;
            lastQuery = query;
            return result;
        }
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<Long, User> byId = new LinkedHashMap<>();
        private final Map<String, User> byEmail = new LinkedHashMap<>();

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(byEmail.get(email));
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return byId.values().stream()
                    .filter(user -> username != null && username.equals(user.getUsername()))
                    .findFirst();
        }

        @Override
        public boolean existsByEmail(String email) {
            return byEmail.containsKey(email);
        }

        @Override
        public boolean existsByUsername(String username) {
            return byId.values().stream().anyMatch(user -> username != null && username.equals(user.getUsername()));
        }

        @Override
        public User save(User user) {
            byId.put(user.getId(), user);
            if (user.getEmail() != null) {
                byEmail.put(user.getEmail(), user);
            }
            return user;
        }

        @Override
        public void delete(User user) {
            if (user != null) {
                byId.remove(user.getId());
                if (user.getEmail() != null) {
                    byEmail.remove(user.getEmail());
                }
            }
        }

        @Override
        public boolean deleteByEmail(String email) {
            return byEmail.remove(email) != null;
        }

        @Override
        public boolean deleteByUsername(String username) {
            Long id = byId.values().stream()
                    .filter(user -> username != null && username.equals(user.getUsername()))
                    .map(User::getId)
                    .findFirst()
                    .orElse(null);
            if (id == null) {
                return false;
            }
            User removed = byId.remove(id);
            if (removed != null && removed.getEmail() != null) {
                byEmail.remove(removed.getEmail());
            }
            return true;
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(byId.values());
        }

        @Override
        public List<User> searchByEmail(String email) {
            return byEmail.values().stream()
                    .filter(user -> email != null && user.getEmail() != null && user.getEmail().contains(email))
                    .toList();
        }

        @Override
        public long count() {
            return byId.size();
        }

        @Override
        public long countByRole(com.uniai.user.domain.valueobject.UserRole role) {
            return byId.values().stream().filter(user -> user.getRole() == role).count();
        }
    }

    private static final class InMemoryChatRepository implements ChatRepository {
        private final Map<Long, Chat> chats = new LinkedHashMap<>();

        @Override
        public Optional<Chat> findById(Long id) {
            return Optional.ofNullable(chats.get(id));
        }

        @Override
        public Optional<Chat> findByIdForUpdate(Long id) {
            return findById(id);
        }

        @Override
        public boolean updateTitleIfAbsent(Long chatId, String title) {
            Chat chat = chats.get(chatId);
            if (chat == null || chat.getTitle() != null) {
                return false;
            }
            chat.setTitle(title);
            return true;
        }

        @Override
        public List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username) {
            return chats.values().stream()
                    .filter(chat -> chat.getUser() != null && username != null && username.equals(chat.getUser().getUsername()))
                    .sorted((left, right) -> {
                        if (left.getUpdatedAt() == null && right.getUpdatedAt() == null) return 0;
                        if (left.getUpdatedAt() == null) return 1;
                        if (right.getUpdatedAt() == null) return -1;
                        return right.getUpdatedAt().compareTo(left.getUpdatedAt());
                    })
                    .toList();
        }

        @Override
        public String findTitleById(Long chatId) {
            Chat chat = chats.get(chatId);
            return chat != null ? chat.getTitle() : null;
        }

        @Override
        public Chat save(Chat chat) {
            chats.put(chat.getId(), chat);
            return chat;
        }

        @Override
        public void delete(Chat chat) {
            if (chat != null) {
                chats.remove(chat.getId());
            }
        }

        @Override
        public void deleteAll(List<Chat> chats) {
            if (chats != null) {
                chats.forEach(chat -> {
                    if (chat != null) {
                        this.chats.remove(chat.getId());
                    }
                });
            }
        }

        @Override
        public long count() {
            return chats.size();
        }

        @Override
        public long countByUserId(Long userId) {
            return chats.values().stream()
                    .filter(chat -> chat.getUser() != null && chat.getUser().getId() != null && chat.getUser().getId().equals(userId))
                    .count();
        }
    }

    private static final class InMemoryMessageRepository implements MessageRepository {
        private final Map<Long, List<Message>> messagesByChatId = new LinkedHashMap<>();

        @Override
        public List<Message> findByChatIdOrderByTimestampAsc(Long chatId) {
            return messagesByChatId.getOrDefault(chatId, List.of()).stream()
                    .sorted((left, right) -> {
                        if (left.getTimestamp() == null && right.getTimestamp() == null) return 0;
                        if (left.getTimestamp() == null) return 1;
                        if (right.getTimestamp() == null) return -1;
                        return left.getTimestamp().compareTo(right.getTimestamp());
                    })
                    .toList();
        }

        @Override
        public List<Message> findTop10ByChatIdOrderByTimestampDesc(Long chatId) {
            return findByChatIdOrderByTimestampAsc(chatId).stream()
                    .sorted((left, right) -> right.getTimestamp().compareTo(left.getTimestamp()))
                    .limit(10)
                    .toList();
        }

        @Override
        public void deleteByChatId(Long chatId) {
            messagesByChatId.remove(chatId);
        }

        @Override
        public void deleteByChatIdIn(List<Long> chatIds) {
            if (chatIds != null) {
                chatIds.forEach(messagesByChatId::remove);
            }
        }

        @Override
        public long countByChatId(Long chatId) {
            return messagesByChatId.getOrDefault(chatId, List.of()).size();
        }

        @Override
        public long count() {
            return messagesByChatId.values().stream().mapToLong(List::size).sum();
        }

        @Override
        public long countByUserId(Long userId) {
            return messagesByChatId.values().stream()
                    .flatMap(List::stream)
                    .filter(message -> message.getSenderId() != null && message.getSenderId().equals(userId))
                    .count();
        }

        @Override
        public boolean existsByChatId(Long chatId) {
            return messagesByChatId.containsKey(chatId) && !messagesByChatId.get(chatId).isEmpty();
        }

        @Override
        public Message save(Message message) {
            Long chatId = message.getChat() != null ? message.getChat().getId() : null;
            if (chatId != null) {
                messagesByChatId.computeIfAbsent(chatId, ignored -> new ArrayList<>()).add(message);
            }
            return message;
        }
    }
}
