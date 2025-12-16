import React, { useState, useEffect, useRef } from 'react';
import { chatService } from '../../services/chat';
import ChatSidebar from '../chat/ChatSidebar';
import ChatMessage from '../chat/ChatMessage';
import ChatInput from '../chat/ChatInput';
import LoadingSpinner from '../common/LoadingSpinner';
import type { MessageResponseDto, SendMessageDto } from '../../types/dto';

const ChatPage: React.FC = () => {
  const [currentChatId, setCurrentChatId] = useState<number | null>(null);
  const [messages, setMessages] = useState<MessageResponseDto[]>([]);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isSendingMessage, setIsSendingMessage] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Scroll to bottom when messages change
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Load messages when chat is selected
  useEffect(() => {
    if (currentChatId) {
      loadMessages(currentChatId);
    }
  }, [currentChatId]);

  const loadMessages = async (chatId: number) => {
    setIsLoadingMessages(true);
    try {
      const data = await chatService.getChatMessages(chatId);
      setMessages(data);
    } catch (error) {
      console.error('Failed to load messages:', error);
    } finally {
      setIsLoadingMessages(false);
    }
  };

  const handleNewChat = async () => {
    try {
      const newChat = await chatService.createChat();
      setCurrentChatId(newChat.id);
      setMessages([]);
    } catch (error) {
      console.error('Failed to create new chat:', error);
    }
  };

  const handleSelectChat = (chatId: number) => {
    setCurrentChatId(chatId);
  };

  const handleDeleteChat = (chatId: number) => {
    if (chatId === currentChatId || chatId === -1) {
      // Current chat was deleted or all chats deleted
      setCurrentChatId(null);
      setMessages([]);
    }
  };

  const handleSendMessage = async (messageText: string) => {
    if (!currentChatId) {
      // Create a new chat if none exists
      await handleNewChat();
      // Wait for chat to be created
      setTimeout(() => handleSendMessage(messageText), 500);
      return;
    }

    setIsSendingMessage(true);

    try {
      const data: SendMessageDto = {
        chatId: currentChatId,
        message: messageText,
      };

      const response = await chatService.sendMessage(data);
      
      // Add user message and AI response to messages
      setMessages(prev => [...prev, response]);
      
    } catch (error) {
      console.error('Failed to send message:', error);
    } finally {
      setIsSendingMessage(false);
    }
  };

  return (
    <div className="flex h-[calc(100vh-64px)]">
      {/* Sidebar */}
      <ChatSidebar
        selectedChatId={currentChatId}
        onSelectChat={handleSelectChat}
        onNewChat={handleNewChat}
        onDeleteChat={handleDeleteChat}
      />

      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col bg-custom-light">
        {/* Messages Area */}
        <div className="flex-1 overflow-y-auto p-6">
          {!currentChatId ? (
            <div className="h-full flex flex-col items-center justify-center text-center">
              <svg
                className="h-20 w-20 text-custom-primary mb-4 opacity-50"
                fill="currentColor"
                viewBox="0 0 54 44"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M26.5816 43.3134L53.1633 0H39.8724L26.5816 26.5816L13.2908 0H0L26.5816 43.3134Z"></path>
              </svg>
              <h2 className="text-2xl font-bold text-[#151514] mb-2">Welcome to uniAI Chat</h2>
              <p className="text-[#797672] mb-6">
                Start a new chat to begin your conversation with AI
              </p>
              <button
                onClick={handleNewChat}
                className="bg-custom-primary text-[#151514] px-6 py-3 rounded-full font-bold hover:bg-[#a69d8f] transition"
              >
                Start New Chat
              </button>
            </div>
          ) : isLoadingMessages ? (
            <div className="h-full flex items-center justify-center">
              <LoadingSpinner text="Loading messages..." />
            </div>
          ) : (
            <div className="max-w-4xl mx-auto">
              {messages.length === 0 ? (
                <div className="text-center text-[#797672] py-8">
                  No messages yet. Start the conversation!
                </div>
              ) : (
                messages.map((message) => (
                  <ChatMessage
                    key={message.id}
                    message={message}
                    isAI={message.senderId === 0}
                  />
                ))
              )}
              {isSendingMessage && (
                <div className="flex justify-start gap-3 mb-4">
                  <div className="flex-shrink-0 w-8 h-8 rounded-full bg-custom-primary flex items-center justify-center">
                    <span className="material-symbols-outlined text-[#151514] text-sm">smart_toy</span>
                  </div>
                  <div className="bg-white/70 px-4 py-3 rounded-2xl rounded-tl-none">
                    <div className="flex gap-1">
                      <span className="w-2 h-2 bg-custom-primary rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></span>
                      <span className="w-2 h-2 bg-custom-primary rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></span>
                      <span className="w-2 h-2 bg-custom-primary rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></span>
                    </div>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>
          )}
        </div>

        {/* Input Area */}
        {currentChatId && (
          <ChatInput
            onSendMessage={handleSendMessage}
            disabled={isSendingMessage}
          />
        )}
      </div>
    </div>
  );
};

export default ChatPage;
