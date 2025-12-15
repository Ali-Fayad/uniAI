package com.uniai.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok. Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageDto {
    @NotNull(message = "Chat ID is required")
    private Long chatId;

    @NotBlank(message = "Content cannot be empty")
    private String content;
}
