package com.uniai.chat.application.port.in;

import com.uniai.chat.application.dto.response.ChatCreationResponseDto;

public interface CreateChatUseCase {
    ChatCreationResponseDto createChat(String email);
}
