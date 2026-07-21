package com.uniai.admin.prompt.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminPromptServiceTest {

    private final AdminPromptService service = new AdminPromptService();

    @Test
    void exposesOnlyTheAllowlistedPromptInventory() {
        assertEquals(4, service.list().size());
        assertFalse(service.get("graduate-route-planner").editable());
    }

    @Test
    void rejectsUnknownPromptKeys() {
        assertThrows(AdminPromptService.PromptNotFoundException.class,
                () -> service.get("../../application.properties"));
    }
}
