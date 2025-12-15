package com.uniai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatCreationResponseDto {
    private Long chatId;
    // Title will be null on creation — no problem! 
    // Frontend fetches it after first message or via separate endpoint
}