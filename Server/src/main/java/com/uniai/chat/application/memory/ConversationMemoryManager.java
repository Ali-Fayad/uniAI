package com.uniai.chat.application.memory;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import com.uniai.chat.application.budget.ConversationMemoryBudgetConfiguration;
import com.uniai.chat.application.budget.ConversationMemoryBudgetManager;
import com.uniai.chat.application.budget.ConversationMemoryBudgetResult;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationResult;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationStatus;
import com.uniai.chat.application.port.out.ConversationMemoryPersistencePort;
import com.uniai.chat.application.port.out.ConversationMemoryPersistencePort.ConversationMemoryState;
import com.uniai.chat.application.port.out.ConversationMemoryPromptPort;
import com.uniai.chat.domain.repository.MessageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationMemoryManager {

    private static final Logger logger = LogManager.getLogger(ConversationMemoryManager.class);

    private final ConversationMemoryPersistencePort persistencePort;
    private final ConversationMemoryUpdatePort updatePort;
    private final ConversationMemoryBudgetManager budgetManager;
    private final ConversationMemoryValidator validator;
    private final ConversationMemoryMergePolicy mergePolicy;
    private final ConversationMemoryTriggerPolicy triggerPolicy;
    private final UniversityCatalogRepository universityCatalogRepository;
    private final MessageRepository messageRepository;
    private final ConversationMemoryPromptPort promptPort;
    private final ConversationMemoryBudgetConfiguration budgetConfiguration;

    public ConversationMemoryManager(
            ConversationMemoryPersistencePort persistencePort,
            ConversationMemoryUpdatePort updatePort,
            ConversationMemoryBudgetManager budgetManager,
            ConversationMemoryValidator validator,
            ConversationMemoryMergePolicy mergePolicy,
            ConversationMemoryTriggerPolicy triggerPolicy,
            UniversityCatalogRepository universityCatalogRepository,
            MessageRepository messageRepository,
            ConversationMemoryPromptPort promptPort,
            ConversationMemoryBudgetConfiguration budgetConfiguration
    ) {
        this.persistencePort = persistencePort;
        this.updatePort = updatePort;
        this.budgetManager = budgetManager;
        this.validator = validator;
        this.mergePolicy = mergePolicy;
        this.triggerPolicy = triggerPolicy;
        this.universityCatalogRepository = universityCatalogRepository;
        this.messageRepository = messageRepository;
        this.promptPort = promptPort;
        this.budgetConfiguration = budgetConfiguration;
    }

    public ConversationMemory loadMemory(Long chatId) {
        if (chatId == null || persistencePort == null) {
            return ConversationMemory.empty();
        }
        ConversationMemoryState state = persistencePort.load(chatId);
        if (state == null || state.memory() == null) {
            return ConversationMemory.empty();
        }
        if (!state.memory().hasValidSchema()) {
            logger.warn("[AI_MEMORY] Unsupported memory schema loaded chatId={} schemaVersion={}", chatId, state.memory().schemaVersion());
            return ConversationMemory.empty();
        }
        return state.memory();
    }

    public void updateMemoryIfNeeded(
            Long chatId,
            ConversationMemory previousMemory,
            String currentUserMessage,
            String assistantResponse,
            GraduateQueryInterpretationResult interpretationResult
    ) {
        if (chatId == null || !isEnabled() || interpretationResult == null) {
            return;
        }

        ConversationMemory baseMemory = previousMemory == null ? ConversationMemory.empty() : previousMemory;
        long completedTurns = messageRepository == null ? 0L : Math.max(0L, messageRepository.countByChatId(chatId) / 2L);
        if (!triggerPolicy.shouldUpdate(baseMemory, interpretationResult, completedTurns, currentUserMessage)) {
            logger.debug("[AI_MEMORY] Memory update skipped chatId={} reason=no-trigger", chatId);
            return;
        }

        ConversationMemoryUpdateRequest request = new ConversationMemoryUpdateRequest(
                baseMemory,
                currentUserMessage,
                assistantResponse,
                interpretationResult
        );

        String prompt = promptPort != null ? promptPort.getPrompt() : "";
        ConversationMemoryBudgetResult budgetResult = budgetManager != null ? budgetManager.budget(request, prompt) : null;
        if (budgetResult != null && !budgetResult.requestFits()) {
            logger.warn("[AI_MEMORY] Memory update skipped chatId={} reason=budget-exceeded category={}",
                    chatId,
                    budgetResult.diagnosticCategory());
            return;
        }

        List<UniversityCatalog> catalogs = universityCatalogRepository == null ? List.of() : universityCatalogRepository.findAll();
        ConversationMemoryPatch patch = proposePatch(request);
        ConversationMemoryValidator.ValidationResult validationResult = validator.validatePatch(patch, catalogs);
        if (!validationResult.isValid()) {
            logger.warn("[AI_MEMORY] Memory patch rejected chatId={} category={}", chatId, validationResult.failureCategory());
            patch = buildDeterministicPatch(interpretationResult);
            validationResult = validator.validatePatch(patch, catalogs);
            if (!validationResult.isValid() && !validationResult.unsupported()) {
                logger.warn("[AI_MEMORY] Deterministic memory patch rejected chatId={} category={}", chatId, validationResult.failureCategory());
                return;
            }
        }

        ConversationMemory merged = mergePolicy.merge(baseMemory, patch, interpretationResult.query());
        persistWithRetry(chatId, merged, patch, interpretationResult);
    }

    private boolean isEnabled() {
        return budgetConfiguration == null || budgetConfiguration.enabled();
    }

    private ConversationMemoryPatch proposePatch(ConversationMemoryUpdateRequest request) {
        if (updatePort == null) {
            return buildDeterministicPatch(request.interpretationResult());
        }
        try {
            ConversationMemoryPatch patch = updatePort.proposeUpdate(request);
            return patch == null ? buildDeterministicPatch(request.interpretationResult()) : patch;
        } catch (RuntimeException ex) {
            logger.warn("[AI_MEMORY] Memory update provider failed reason={}", ex.getMessage());
            return buildDeterministicPatch(request.interpretationResult());
        }
    }

    private ConversationMemoryPatch buildDeterministicPatch(GraduateQueryInterpretationResult interpretationResult) {
        if (interpretationResult == null || interpretationResult.query() == null) {
            return emptyPatch();
        }

        List<String> universities = interpretationResult.query().resolvedUniversities().stream()
                .map(university -> university == null ? null : universeMention(university))
                .filter(value -> value != null && !value.isBlank())
                .toList();
        List<String> degrees = interpretationResult.query().degreeTypes() == null ? List.of() : interpretationResult.query().degreeTypes();
        boolean comparisonActive = interpretationResult.query().followUpResolved() && interpretationResult.query().resolvedUniversities().size() > 1;

        return new ConversationMemoryPatch(
                ConversationMemory.SCHEMA_VERSION,
                interpretationResult.query().intent() != null ? interpretationResult.query().intent().name() : null,
                comparisonActive,
                universities,
                List.of(),
                List.of(),
                degrees,
                List.of(),
                List.of(),
                universities,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of()
        );
    }

    private ConversationMemoryPatch emptyPatch() {
        return new ConversationMemoryPatch(
                ConversationMemory.SCHEMA_VERSION,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of()
        );
    }

    private void persistWithRetry(Long chatId, ConversationMemory merged, ConversationMemoryPatch patch, GraduateQueryInterpretationResult interpretationResult) {
        if (chatId == null || persistencePort == null || merged == null) {
            return;
        }

        ConversationMemoryState currentState = persistencePort.load(chatId);
        long expectedVersion = currentState == null ? 0L : currentState.memoryVersion();
        if (persistencePort.save(chatId, expectedVersion, merged, LocalDateTime.now())) {
            logger.debug("[AI_MEMORY] Memory updated chatId={} version={} size={}", chatId, expectedVersion + 1, merged.toPromptText().length());
            return;
        }

        ConversationMemoryState refreshedState = persistencePort.load(chatId);
        long refreshedVersion = refreshedState == null ? 0L : refreshedState.memoryVersion();
        ConversationMemory retryBase = refreshedState == null || refreshedState.memory() == null ? ConversationMemory.empty() : refreshedState.memory();
        ConversationMemory retried = mergePolicy.merge(retryBase, patch, interpretationResult != null ? interpretationResult.query() : null);
        if (persistencePort.save(chatId, refreshedVersion, retried, LocalDateTime.now())) {
            logger.debug("[AI_MEMORY] Memory updated after retry chatId={} version={}", chatId, refreshedVersion + 1);
        } else {
            logger.warn("[AI_MEMORY] Memory update skipped after conflict chatId={}", chatId);
        }
    }

    private String universeMention(com.uniai.chat.application.retrieval.ResolvedUniversity university) {
        if (university == null) {
            return null;
        }
        if (university.acronym() != null && !university.acronym().isBlank()) {
            return university.acronym();
        }
        return university.name();
    }
}
