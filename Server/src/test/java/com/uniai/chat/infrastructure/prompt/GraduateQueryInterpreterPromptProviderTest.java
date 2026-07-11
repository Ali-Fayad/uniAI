package com.uniai.chat.infrastructure.prompt;

import com.uniai.chat.infrastructure.config.GraduateQueryInterpretationProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateQueryInterpreterPromptProviderTest {

    @Test
    void shouldLoadDedicatedInterpretationPrompt() {
        GraduateQueryInterpretationProperties properties = new GraduateQueryInterpretationProperties();
        properties.setPromptPath("prompts/graduate-query-interpreter-prompt.txt");

        GraduateQueryInterpreterPromptProvider provider = new GraduateQueryInterpreterPromptProvider(properties);

        assertTrue(provider.getPrompt().contains("schemaVersion 1"));
        assertTrue(provider.getPrompt().contains("PROGRAM_LOOKUP"));
    }

    @Test
    void shouldFailClearlyWhenPromptIsMissing() {
        GraduateQueryInterpretationProperties properties = new GraduateQueryInterpretationProperties();
        properties.setPromptPath("prompts/does-not-exist.txt");

        assertThrows(IllegalStateException.class, () -> new GraduateQueryInterpreterPromptProvider(properties));
    }
}
