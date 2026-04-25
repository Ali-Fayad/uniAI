package com.uniai.chat.application.port.in;

import java.util.List;

import com.uniai.chat.application.dto.response.ChatSummaryResponseDto;

public interface GetUserChatsUseCase {
    List<ChatSummaryResponseDto> getUserChats(String email);
}
