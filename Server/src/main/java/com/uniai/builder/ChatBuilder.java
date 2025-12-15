package com.uniai.builder;

import java.time.LocalDateTime;

import com.uniai.dto.ChatCreationResponseDto;
import com.uniai.dto.MessageResponseDto;
import com.uniai.dto.SendMessageDto;
import com.uniai.model.Chat;
import com.uniai.model.Message;

public class ChatBuilder {

    public static ChatCreationResponseDto toChatCreationResponse(Chat chat) {
        return ChatCreationResponseDto.builder()
                .chatId(chat. getId())
                .build();
    }

    public static MessageResponseDto toMessageResponse(Message message) {
        return MessageResponseDto. builder()
                .messageId(message.getId())
                .chatId(message.getChatId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }

        public static Message buildUserMessage(SendMessageDto dto, Long userId) {
        return Message.builder()
                .chatId(dto.getChatId())
                .senderId(userId)
                .content(dto.getContent())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Build an AI response message. 
     */
    public static Message buildAIMessage(Long chatId, String aiResponseContent) {
        return Message. builder()
                .chatId(chatId)
                .senderId(0L) // 0 = AI
                .content(aiResponseContent)
                .timestamp(LocalDateTime.now())
                .build();
    }
}