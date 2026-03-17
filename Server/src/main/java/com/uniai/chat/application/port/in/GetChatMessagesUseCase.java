package com.uniai.chat.application.port.in;

import com.uniai.chat.application.dto.response.MessageResponseDto;

import java.util.List;

public interface GetChatMessagesUseCase {
    List<MessageResponseDto> getChatMessages(String email, Long chatId);
}
