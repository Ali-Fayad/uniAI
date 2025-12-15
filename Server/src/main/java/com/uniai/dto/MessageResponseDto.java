package com.uniai.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok. Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDto {
    private Long messageId;
    private Long chatId;
    private Long senderId; // 0 = AI, user ID = user
    private String content;
    private LocalDateTime timestamp;
    // NO title here — frontend gets it separately if needed
}