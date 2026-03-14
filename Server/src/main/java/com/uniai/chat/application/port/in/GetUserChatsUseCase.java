package com.uniai.chat.application.port.in;

import com.uniai.chat.domain.model.Chat;

import java.util.List;

public interface GetUserChatsUseCase {
    List<Chat> getUserChats(String email);
}
