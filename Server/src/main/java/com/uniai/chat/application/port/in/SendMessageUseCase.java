package com.uniai.chat.application.port.in;

import com.uniai.chat.application.dto.command.SendMessageCommand;
import com.uniai.chat.application.dto.response.MessageResponseDto;

public interface SendMessageUseCase {
    /** Persists the user message, generates an AI response, and returns it. */
    MessageResponseDto sendMessage(String email, SendMessageCommand command);
}
