/**
 * useChat Hook
 *
 * Encapsulates all chat-related business logic and state management.
 * Separates concerns from ChatPage component.
 */

import { useEffect, useRef, useState } from "react";
import { chatService } from "../services/chat";
import { useAuth } from "./useAuth";
import type { MessageResponseDto, SendMessageDto } from "../types/dto";

export const useChat = () => {
  const { user } = useAuth();

  const [currentChatId, setCurrentChatId] = useState<number | null>(null);
  const [messages, setMessages] = useState<MessageResponseDto[]>([]);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isSendingMessage, setIsSendingMessage] = useState(false);
  const [streamingMessageId, setStreamingMessageId] = useState<number | null>(
    null,
  );

  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Prevent a newly created chat from immediately reloading an empty history
  // and overwriting the optimistic first user message.
  const skipNextMessageLoadRef = useRef(false);

  // Scroll to bottom when messages change
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isSendingMessage]);

  // Load messages when an existing chat is selected
  useEffect(() => {
    if (!currentChatId) {
      setMessages([]);
      setStreamingMessageId(null);
      return;
    }

    if (skipNextMessageLoadRef.current) {
      skipNextMessageLoadRef.current = false;
      return;
    }

    void loadMessages(currentChatId);
  }, [currentChatId]);

  const loadMessages = async (chatId: number) => {
    setIsLoadingMessages(true);
    setStreamingMessageId(null);

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
    skipNextMessageLoadRef.current = false;
    setStreamingMessageId(null);
    setCurrentChatId(null);
    setMessages([]);
  };

  const handleSelectChat = (chatId: number) => {
    if (chatId !== currentChatId) {
      skipNextMessageLoadRef.current = false;
      setStreamingMessageId(null);
      setCurrentChatId(chatId);
    }
  };

  const handleDeleteChat = (chatId: number) => {
    if (chatId === currentChatId || chatId === -1) {
      skipNextMessageLoadRef.current = false;
      setStreamingMessageId(null);
      setCurrentChatId(null);
      setMessages([]);
    }
  };

  const handleSendMessage = async (content: string) => {
    const trimmedContent = content.trim();

    if (!trimmedContent) {
      return;
    }

    setIsSendingMessage(true);
    setStreamingMessageId(null);

    const tempUserMessage: MessageResponseDto = {
      messageId: Date.now(),
      chatId: currentChatId ?? 0,
      senderId: user?.id ?? 1,
      content: trimmedContent,
      timestamp: new Date().toISOString(),
      citations: [],
    };

    setMessages((previousMessages) => [
      ...previousMessages,
      tempUserMessage,
    ]);

    let targetChatId = currentChatId;

    try {
      if (!targetChatId) {
        const newChat = await chatService.createChat();
        targetChatId = newChat.chatId;

        skipNextMessageLoadRef.current = true;
        setCurrentChatId(targetChatId);
      }

      const data: SendMessageDto = {
        chatId: targetChatId,
        content: trimmedContent,
      };

      const response = await chatService.sendMessage(data);

      setMessages((previousMessages) => [
        ...previousMessages,
        response,
      ]);
      setStreamingMessageId(response.messageId);
    } catch (error) {
      console.error("Failed to send message:", error);

      const fallbackMessage: MessageResponseDto = {
        messageId: Date.now() + 1,
        chatId: targetChatId ?? 0,
        senderId: 0,
        content:
          "Something went wrong while generating the response. Please try again later.",
        timestamp: new Date().toISOString(),
        citations: [],
      };

      setMessages((previousMessages) => [
        ...previousMessages,
        fallbackMessage,
      ]);
      setStreamingMessageId(fallbackMessage.messageId);
    } finally {
      setIsSendingMessage(false);
    }
  };

  return {
    currentChatId,
    messages,
    isLoadingMessages,
    isSendingMessage,
    streamingMessageId,
    messagesEndRef,
    handleNewChat,
    handleSelectChat,
    handleDeleteChat,
    handleSendMessage,
  };
};