import React from "react";
import { motion } from "framer-motion";
import { useChat } from "../../hooks/useChat";
import { TEXT } from "../../constants/static";
import ChatSidebar from "../chat/ChatSidebar";
import ChatMessage from "../chat/ChatMessage";
import ChatInput from "../chat/ChatInput";
import TypingIndicator from "../chat/TypingIndicator";
import LoadingSpinner from "../common/LoadingSpinner";
import { PageTransition, StaggerContainer, staggerItemVariants } from "../animations";

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
    <PageTransition>
      <div className="flex h-[calc(100vh-64px)]">
        {/* Sidebar */}
        <motion.div 
          initial={{ x: -20, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          transition={{ duration: 0.4, ease: "easeOut" }}
          className="h-full"
        >
          <ChatSidebar
            selectedChatId={currentChatId}
            onSelectChat={handleSelectChat}
            onNewChat={handleNewChat}
            onDeleteChat={handleDeleteChat}
          />
        </motion.div>

        {/* Main Chat Area */}
        <motion.div 
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="flex-1 flex flex-col bg-[var(--color-background)] relative"
        >
          {/* Messages Area */}
          <div className="flex-1 overflow-y-auto p-6 pb-32">
            {isLoadingMessages ? (
              <div className="h-full flex items-center justify-center">
                <LoadingSpinner text={TEXT.chat.loading} />
              </div>
            ) : (
              <div className="max-w-4xl mx-auto">
                {messages.length === 0 && !currentChatId ? (
                  <motion.div 
                    initial={{ opacity: 0, scale: 0.9 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ delay: 0.3 }}
                    className="flex flex-col items-center justify-center h-full py-20 opacity-50"
                  >
                    <span className="material-symbols-outlined text-6xl mb-4 text-[var(--color-primary)]">
                      {TEXT.chat.emptyState.icon}
                    </span>
                    <p className="text-xl font-medium">
                      {TEXT.chat.emptyState.message}
                    </p>
                  </motion.div>
                ) : (
                  <>
                    <StaggerContainer staggerDelay={0.05} initialDelay={0}>
                      {messages.map((message, index) => (
                        <motion.div key={message.messageId} variants={staggerItemVariants}>
                          <ChatMessage
                            message={message}
                            isAI={message.senderId === 0}
                            index={index}
                          />
                        </motion.div>
                      ))}
                    </StaggerContainer>
                    {isSendingMessage && <TypingIndicator />}
                  </>
                )}
                <div ref={messagesEndRef} />
              </div>
            )}
          </div>

          {/* Input Area - Fixed at bottom */}
          <motion.div 
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.4 }}
            className="absolute bottom-0 left-0 right-0"
          >
            <ChatInput
              onSendMessage={handleSendMessage}
              disabled={isSendingMessage}
            />
          </motion.div>
        </motion.div>
      </div>
    </PageTransition>
  );
};

export default ChatPage;
