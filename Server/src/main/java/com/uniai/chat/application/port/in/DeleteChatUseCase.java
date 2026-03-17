package com.uniai.chat.application.port.in;

public interface DeleteChatUseCase {
    void deleteChat(String email, Long chatId);
}
