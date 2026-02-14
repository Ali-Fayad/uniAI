/**
 * useChat Hook
 * 
 * Encapsulates all chat-related business logic and state management.
 * Separates concerns from ChatPage component.
 */

import { useState, useEffect, useRef, useContext } from "react";
import { chatService } from "../services/chat";
import { AuthContext } from "../context/AuthContext";
import type { MessageResponseDto, SendMessageDto } from "../types/dto";

export const useChat = () => {
  const { user } = useContext(AuthContext)!;
  const [currentChatId, setCurrentChatId] = useState<number | null>(null);
  const [messages, setMessages] = useState<MessageResponseDto[]>([]);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isSendingMessage, setIsSendingMessage] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Scroll to bottom when messages change
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isSendingMessage]);

  // Load messages when chat is selected
  useEffect(() => {
    if (currentChatId) {
      loadMessages(currentChatId);
    } else {
      setMessages([]);
    }
  }, [currentChatId]);

  const loadMessages = async (chatId: number) => {
    setIsLoadingMessages(true);
    try {
      const data = await chatService.getChatMessages(chatId);
      setMessages(data);
    } catch (error) {
      console.error("Failed to load messages:", error);
    } finally {
      setIsLoadingMessages(false);
    }
  };

  const handleNewChat = () => {
    setCurrentChatId(null);
    setMessages([]);
  };

  const handleSelectChat = (chatId: number) => {
    if (chatId !== currentChatId) {
      setCurrentChatId(chatId);
    }
  };

  const handleDeleteChat = (chatId: number) => {
    if (chatId === currentChatId || chatId === -1) {
      // Current chat was deleted or all chats deleted
      setCurrentChatId(null);
      setMessages([]);
    }
  };

  const handleSendMessage = async (content: string) => {
    if (!content.trim()) return;

    setIsSendingMessage(true);

    // Optimistic update: Add user message immediately
    const tempUserMessage: MessageResponseDto = {
      messageId: Date.now(), // Temporary ID
      chatId: currentChatId || 0,
      senderId: user?.id || 1,
      content: content,
      timestamp: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, tempUserMessage]);

    try {
      let targetChatId = currentChatId;

      // If no chat exists, create one first
      if (!targetChatId) {
        const newChat = await chatService.createChat();
        targetChatId = newChat.chatId;
        setCurrentChatId(targetChatId);
      }

      const data: SendMessageDto = {
        chatId: targetChatId,
        content: content,
      };

      const response = await chatService.sendMessage(data);

      // Add AI response to messages
      setMessages((prev) => [...prev, response]);
    } catch (error) {
      console.error("Failed to send message:", error);
      // Optionally remove the optimistic message or show error
    } finally {
      setIsSendingMessage(false);
    }
  };

  return {
    currentChatId,
    messages,
    isLoadingMessages,
    isSendingMessage,
    messagesEndRef,
    handleNewChat,
    handleSelectChat,
    handleDeleteChat,
    handleSendMessage,
  };
};
