package com.uniai.chat.presentation.controller;

import com.uniai.chat.application.dto.command.SendMessageCommand;
import com.uniai.chat.application.dto.response.ChatCreationResponseDto;
import com.uniai.chat.application.dto.response.MessageResponseDto;
import com.uniai.chat.application.port.in.*;
import com.uniai.chat.domain.model.Chat;
import com.uniai.shared.infrastructure.jwt.JwtFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles all chat endpoints.
 * Injects only the fine-grained use-case interfaces it actually calls (ISP).
 */
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final JwtFacade jwtFacade;
    private final CreateChatUseCase createChatUseCase;
    private final SendMessageUseCase sendMessageUseCase;
    private final GetUserChatsUseCase getUserChatsUseCase;
    private final GetChatMessagesUseCase getChatMessagesUseCase;
    private final DeleteChatUseCase deleteChatUseCase;
    private final DeleteAllChatsUseCase deleteAllChatsUseCase;

    @PostMapping
    public ResponseEntity<ChatCreationResponseDto> createChat() {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(createChatUseCase.createChat(email));
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageResponseDto> sendMessage(@Valid @RequestBody SendMessageCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(sendMessageUseCase.sendMessage(email, command));
    }

    @GetMapping
    public ResponseEntity<List<Chat>> getUserChats() {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(getUserChatsUseCase.getUserChats(email));
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<MessageResponseDto>> getChatMessages(@PathVariable Long chatId) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(getChatMessagesUseCase.getChatMessages(email, chatId));
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<MessageResponse> deleteChat(@PathVariable Long chatId) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        deleteChatUseCase.deleteChat(email, chatId);
        return ResponseEntity.ok(new MessageResponse("Chat deleted successfully"));
    }

    @DeleteMapping
    public ResponseEntity<MessageResponse> deleteAllChats() {
        String email = jwtFacade.getAuthenticatedUserEmail();
        deleteAllChatsUseCase.deleteAllChats(email);
        return ResponseEntity.ok(new MessageResponse("All chats deleted successfully"));
    }

    private record MessageResponse(String message) {}
}
