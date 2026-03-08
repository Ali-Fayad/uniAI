import React, { useEffect, useRef } from "react";
import { useChat } from "../../hooks/useChat";
import { useAuthState } from "../../hooks/useAuthState";
import ChatSidebar from "../chat/ChatSidebar";
import ChatMessage from "../chat/ChatMessage";
import ChatInput from "../chat/ChatInput";
import LoadingSpinner from "../common/LoadingSpinner";

/**
 * DIP: ChatPage no longer imports the concrete chatService.
 * It depends on the useChat hook's interface so the data layer can be swapped
 * (e.g. mocked in tests) without touching this component.
 *
 * ISP: uses useAuthState (read-only) instead of the full useAuth.
 * LSP: replaced the unsafe useContext(AuthContext)! with the type-safe hook.
 */
const ChatPage: React.FC = () => {
  const { user } = useAuthState();
  const {
    messages,
    currentChatId,
    isLoadingMessages,
    isSendingMessage,
    selectChat,
    loadMessages,
    sendMessage,
    clearMessages,
  } = useChat();

  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Scroll to bottom whenever messages update
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isSendingMessage]);

  // Load messages whenever the selected chat changes
  useEffect(() => {
    if (currentChatId) {
      loadMessages(currentChatId);
    }
  }, [currentChatId, loadMessages]);

  const handleNewChat = () => clearMessages();

  const handleSelectChat = (chatId: number) => {
    if (chatId !== currentChatId) selectChat(chatId);
  };

  const handleDeleteChat = (chatId: number) => {
    if (chatId === currentChatId || chatId === -1) clearMessages();
  };

  const handleSendMessage = (content: string) => {
    sendMessage(content, user?.id ?? 0);
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
      <div className="flex-1 flex flex-col bg-[var(--color-background)] relative">
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
                  <span className="material-symbols-outlined text-6xl mb-4 text-[var(--color-primary)]">
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
