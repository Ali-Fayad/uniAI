import { useState, useCallback } from 'react';
import { chatService } from '../services/chat';
import type { MessageResponseDto } from '../types/chat';

/**
 * DIP: ChatPage depends on this hook's interface, not on the concrete
 * chatService implementation.  Swapping the HTTP client (e.g. for a mock in
 * tests) only requires changing this one file, not every consumer.
 *
 * SRP: the hook is responsible only for chat data-fetching and local state.
 * UI rendering is left entirely to the component.
 */
export interface UseChatReturn {
  messages: MessageResponseDto[];
  currentChatId: number | null;
  isLoadingMessages: boolean;
  isSendingMessage: boolean;
  selectChat: (chatId: number | null) => void;
  loadMessages: (chatId: number) => Promise<void>;
  sendMessage: (content: string, userId: number) => Promise<void>;
  clearMessages: () => void;
}

export function useChat(): UseChatReturn {
  const [currentChatId, setCurrentChatId] = useState<number | null>(null);
  const [messages, setMessages] = useState<MessageResponseDto[]>([]);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isSendingMessage, setIsSendingMessage] = useState(false);

  const loadMessages = useCallback(async (chatId: number) => {
    setIsLoadingMessages(true);
    try {
      const data = await chatService.getChatMessages(chatId);
      setMessages(data);
    } catch (error) {
      console.error('Failed to load messages:', error);
    } finally {
      setIsLoadingMessages(false);
    }
  }, []);

  const selectChat = useCallback((chatId: number | null) => {
    setCurrentChatId(chatId);
    if (!chatId) setMessages([]);
  }, []);

  const clearMessages = useCallback(() => {
    setMessages([]);
    setCurrentChatId(null);
  }, []);

  const sendMessage = useCallback(
    async (content: string, userId: number) => {
      if (!content.trim()) return;

      setIsSendingMessage(true);

      // Optimistic UI: add user message immediately
      const tempMessage: MessageResponseDto = {
        messageId: Date.now(),
        chatId: currentChatId ?? 0,
        senderId: userId,
        content,
        timestamp: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, tempMessage]);

      try {
        let targetChatId = currentChatId;

        if (!targetChatId) {
          const newChat = await chatService.createChat();
          targetChatId = newChat.chatId;
          setCurrentChatId(targetChatId);
        }

        const response = await chatService.sendMessage({
          chatId: targetChatId,
          content,
        });

        setMessages((prev) => [...prev, response]);
      } catch (error) {
        console.error('Failed to send message:', error);
        // Remove optimistic message on failure
        setMessages((prev) =>
          prev.filter((m) => m.messageId !== tempMessage.messageId),
        );
      } finally {
        setIsSendingMessage(false);
      }
    },
    [currentChatId],
  );

  return {
    messages,
    currentChatId,
    isLoadingMessages,
    isSendingMessage,
    selectChat,
    loadMessages,
    sendMessage,
    clearMessages,
  };
}
