package com.uniai.admin.presentation.controller;

import com.uniai.admin.application.service.AdminApplicationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminControllerTest {

    @Test
    void healthShouldReturnAdminAccessGrantedMessage() {
        AdminController controller = new AdminController(new AdminApplicationService());

        AdminController.AdminHealthResponse response = controller.health().getBody();

        assertEquals("Admin access granted", response.message());
    }
}
