package com.uniai.user.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleAuthCallbackCommand(
        @NotBlank @Size(max = 8192) String code,
        @NotBlank @Size(max = 2048) String redirectUri
) {}
