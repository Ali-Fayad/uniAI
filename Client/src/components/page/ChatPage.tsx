import React, { useState, useEffect, useRef, useContext } from "react";
import { chatService } from "../../services/chat";
import ChatSidebar from "../chat/ChatSidebar";
import ChatMessage from "../chat/ChatMessage";
import ChatInput from "../chat/ChatInput";
import LoadingSpinner from "../common/LoadingSpinner";
import { AuthContext } from "../../context/AuthContext";
import type { MessageResponseDto, SendMessageDto } from "../../types/dto";

const ChatPage: React.FC = () => {
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

  return (
    <div className="flex h-screen">
      {/* Sidebar */}
      <ChatSidebar
        selectedChatId={currentChatId}
        onSelectChat={handleSelectChat}
        onNewChat={handleNewChat}
        onDeleteChat={handleDeleteChat}
      />

      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col bg-custom-light relative">
        {/* Messages Area */}
        <div className="flex-1 overflow-y-auto p-6 pb-32">
          {isLoadingMessages ? (
            <div className="h-full flex items-center justify-center">
              <LoadingSpinner text="Loading messages..." />
            </div>
          ) : (
            <div className="max-w-4xl mx-auto">
              {messages.length === 0 && !currentChatId ? (
                <div className="flex flex-col items-center justify-center h-full py-20 opacity-50">
                  <span className="material-symbols-outlined text-6xl mb-4 text-custom-primary">
                    chat_bubble_outline
                  </span>
                  <p className="text-xl font-medium">
                    Start a new conversation
                  </p>
                </div>
              ) : (
                <>
                  {messages.map((message) => (
                    <ChatMessage
                      key={message.messageId}
                      message={message}
                      isAI={message.senderId === 0}
                    />
                  ))}
                  {isSendingMessage && (
                    <div className="flex justify-start gap-3 mb-4 animate-fadeIn">
                      <div className="flex-shrink-0 w-8 h-8 rounded-full bg-custom-primary flex items-center justify-center">
                        <span className="material-symbols-outlined text-[#151514] text-sm">
                          smart_toy
                        </span>
                      </div>
                      <div className="bg-white/70 px-4 py-3 rounded-2xl rounded-tl-none shadow-sm">
                        <div className="flex gap-1 items-center h-5">
                          <span
                            className="w-2 h-2 bg-custom-primary rounded-full animate-bounce"
                            style={{ animationDelay: "0ms" }}
                          ></span>
                          <span
                            className="w-2 h-2 bg-custom-primary rounded-full animate-bounce"
                            style={{ animationDelay: "150ms" }}
                          ></span>
                          <span
                            className="w-2 h-2 bg-custom-primary rounded-full animate-bounce"
                            style={{ animationDelay: "300ms" }}
                          ></span>
                        </div>
                      </div>
                    </div>
                  )}
                </>
              )}
              <div ref={messagesEndRef} />
            </div>
          )}
        </div>

        {/* Input Area - Fixed at bottom */}
        <div className="absolute bottom-0 left-0 right-0">
          <ChatInput
            onSendMessage={handleSendMessage}
            disabled={isSendingMessage}
          />
        </div>
      </div>
    </div>
  );
};

export default ChatPage;
