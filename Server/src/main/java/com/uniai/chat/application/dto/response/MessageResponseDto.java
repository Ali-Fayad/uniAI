package com.uniai.chat.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDto {
    private Long messageId;
    private Long chatId;
    private Long senderId;
    private String content;
    private LocalDateTime timestamp;
}
