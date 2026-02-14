import React from "react";
import { useChat } from "../../hooks/useChat";
import { TEXT } from "../../constants/static";
import ChatSidebar from "../chat/ChatSidebar";
import ChatMessage from "../chat/ChatMessage";
import ChatInput from "../chat/ChatInput";
import LoadingSpinner from "../common/LoadingSpinner";

/**
 * ChatPage Component
 * 
 * Responsibilities:
 * - Render chat interface layout
 * - Compose chat components (sidebar, messages, input)
 * 
 * All business logic is encapsulated in useChat hook.
 */
const ChatPage: React.FC = () => {
  const {
    currentChatId,
    messages,
    isLoadingMessages,
    isSendingMessage,
    messagesEndRef,
    handleNewChat,
    handleSelectChat,
    handleDeleteChat,
    handleSendMessage,
  } = useChat();

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
      <div className="flex-1 flex flex-col bg-[var(--color-background)] relative">
        {/* Messages Area */}
        <div className="flex-1 overflow-y-auto p-6 pb-32">
          {isLoadingMessages ? (
            <div className="h-full flex items-center justify-center">
              <LoadingSpinner text={TEXT.chat.loading} />
            </div>
          ) : (
            <div className="max-w-4xl mx-auto">
              {messages.length === 0 && !currentChatId ? (
                <div className="flex flex-col items-center justify-center h-full py-20 opacity-50">
                  <span className="material-symbols-outlined text-6xl mb-4 text-[var(--color-primary)]">
                    {TEXT.chat.emptyState.icon}
                  </span>
                  <p className="text-xl font-medium">
                    {TEXT.chat.emptyState.message}
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
                      <div className="flex-shrink-0 w-8 h-8 rounded-full bg-[var(--color-primary)] flex items-center justify-center">
                        <span className="material-symbols-outlined text-[var(--color-background)] text-sm">
                          smart_toy
                        </span>
                      </div>
                      <div className="bg-[var(--color-surface)] px-4 py-3 rounded-2xl rounded-tl-none shadow-sm">
                        <div className="flex gap-1 items-center h-5">
                          <span
                            className="w-2 h-2 bg-[var(--color-primary)] rounded-full animate-bounce"
                            style={{ animationDelay: "0ms" }}
                          ></span>
                          <span
                            className="w-2 h-2 bg-[var(--color-primary)] rounded-full animate-bounce"
                            style={{ animationDelay: "150ms" }}
                          ></span>
                          <span
                            className="w-2 h-2 bg-[var(--color-primary)] rounded-full animate-bounce"
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
