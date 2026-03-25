package com.uniai.user.presentation.controller;

import com.uniai.user.application.port.in.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignUpUseCase signUpUseCase;

    @MockBean
    private SignInUseCase signInUseCase;

    @MockBean
    private VerifyEmailUseCase verifyEmailUseCase;

    @MockBean
    private VerifyTwoFactorUseCase verifyTwoFactorUseCase;

    @MockBean
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @MockBean
    private ConfirmPasswordResetUseCase confirmPasswordResetUseCase;

    @MockBean
    private GetGoogleAuthUrlUseCase getGoogleAuthUrlUseCase;

    @MockBean
    private CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase;

    @Test
    void checkEmailShouldReturnAvailabilityPayload() throws Exception {
        when(checkEmailAvailabilityUseCase.isEmailAvailable(eq("new@example.com"))).thenReturn(true);

        mockMvc.perform(get("/api/auth/check-email").param("email", "new@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("Email available"));
    }
}
