package com.uniai.chat.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageCommand {

    @NotNull(message = "Chat ID is required")
    private Long chatId;

    @NotBlank(message = "Content cannot be empty")
    private String content;
}
