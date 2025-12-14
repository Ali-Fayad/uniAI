package com. uniai.controller;

import com.uniai.dto.ChatCreationResponseDto;
import com. uniai.dto.MessageResponseDto;
import com.uniai. dto.SendMessageDto;
import com.uniai.model.Chat;
import com.uniai.security.JwtUtil;
import com.uniai. services.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework. http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    /**
     * Create a new chat for the authenticated user.
     * POST /api/chats
     */
    @PostMapping
    public ResponseEntity<ChatCreationResponseDto> createChat(
            @RequestHeader("Authorization") String authHeader) {
        
        String email = jwtUtil.extractEmailFromAuthorizationHeader(authHeader);
        ChatCreationResponseDto response = chatService.createChat(email);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Send a message to a chat and get AI response.
     * POST /api/chats/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<MessageResponseDto> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SendMessageDto dto) {
        
        String email = jwtUtil.extractEmailFromAuthorizationHeader(authHeader);
        MessageResponseDto response = chatService.sendMessage(email, dto);
        
        return ResponseEntity. ok(response);
    }

    /**
     * Get all chats for the authenticated user.
     * GET /api/chats
     */
    @GetMapping
    public ResponseEntity<List<Chat>> getUserChats(
            @RequestHeader("Authorization") String authHeader) {
        
        String email = jwtUtil.extractEmailFromAuthorizationHeader(authHeader);
        List<Chat> chats = chatService.getUserChats(email);
        
        return ResponseEntity. ok(chats);
    }

    /**
     * Get all messages in a specific chat.
     * GET /api/chats/{chatId}/messages
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<MessageResponseDto>> getChatMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long chatId) {
        
        String email = jwtUtil. extractEmailFromAuthorizationHeader(authHeader);
        List<MessageResponseDto> messages = chatService. getChatMessages(email, chatId);
        
        return ResponseEntity.ok(messages);
    }

    /**
     * Delete a specific chat and all its messages. 
     * DELETE /api/chats/{chatId}
     */
    @DeleteMapping("/{chatId}")
    public ResponseEntity<MessageResponse> deleteChat(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long chatId) {
        
        String email = jwtUtil.extractEmailFromAuthorizationHeader(authHeader);
        chatService.deleteChat(email, chatId);
        
        return ResponseEntity.ok(new MessageResponse("Chat deleted successfully"));
    }

    /**
     * Delete all chats for the authenticated user.
     * DELETE /api/chats
     */
    @DeleteMapping
    public ResponseEntity<MessageResponse> deleteAllChats(
            @RequestHeader("Authorization") String authHeader) {
        
        String email = jwtUtil.extractEmailFromAuthorizationHeader(authHeader);
        chatService.deleteAllChats(email);
        
        return ResponseEntity.ok(new MessageResponse("All chats deleted successfully"));
    }

    // Response record for simple messages
    private record MessageResponse(String message) {
    }
}