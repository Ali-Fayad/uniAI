import React, { useCallback, useEffect, useRef, useState } from "react";
import { motion } from "framer-motion";
import { useChat } from "../../hooks/useChat";
import { TEXT } from "../../constants/static";
import ChatSidebar from "../chat/ChatSidebar";
import ChatMessage from "../chat/ChatMessage";
import ChatInput from "../chat/ChatInput";
import TypingIndicator from "../chat/TypingIndicator";
import LoadingSpinner from "../common/LoadingSpinner";
import {
  PageTransition,
  StaggerContainer,
  staggerItemVariants,
} from "../animations";

const DESKTOP_SIDEBAR_MIN_WIDTH = 240;
const DESKTOP_SIDEBAR_MAX_WIDTH = 480;
const DESKTOP_SIDEBAR_DEFAULT_WIDTH = 320;
const SIDEBAR_WIDTH_STORAGE_KEY = "chat.sidebar.width";
const SIDEBAR_COLLAPSED_STORAGE_KEY = "chat.sidebar.collapsed";

const clampSidebarWidth = (width: number) =>
  Math.min(
    DESKTOP_SIDEBAR_MAX_WIDTH,
    Math.max(DESKTOP_SIDEBAR_MIN_WIDTH, width),
  );

const readStoredSidebarWidth = () => {
  if (typeof window === "undefined") {
    return DESKTOP_SIDEBAR_DEFAULT_WIDTH;
  }

  const storedWidth = window.localStorage.getItem(SIDEBAR_WIDTH_STORAGE_KEY);
  const parsedWidth = storedWidth === null ? Number.NaN : Number(storedWidth);
  return Number.isFinite(parsedWidth)
    ? clampSidebarWidth(parsedWidth)
    : DESKTOP_SIDEBAR_DEFAULT_WIDTH;
};

const readStoredSidebarCollapsed = () => {
  if (typeof window === "undefined") {
    return false;
  }

  return window.localStorage.getItem(SIDEBAR_COLLAPSED_STORAGE_KEY) === "true";
};

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
  const [desktopSidebarWidth, setDesktopSidebarWidth] = useState(
    readStoredSidebarWidth,
  );
  const [isDesktopSidebarCollapsed, setIsDesktopSidebarCollapsed] = useState(
    readStoredSidebarCollapsed,
  );
  const [isDraggingDesktopSidebar, setIsDraggingDesktopSidebar] =
    useState(false);
  const sidebarDragCleanupRef = useRef<(() => void) | null>(null);
  const previousDesktopSidebarWidthRef = useRef(desktopSidebarWidth);

  const {
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
  } = useChat();

  useEffect(() => {
    previousDesktopSidebarWidthRef.current = clampSidebarWidth(
      desktopSidebarWidth,
    );
  }, [desktopSidebarWidth]);

  useEffect(() => {
    window.localStorage.setItem(
      SIDEBAR_WIDTH_STORAGE_KEY,
      String(clampSidebarWidth(desktopSidebarWidth)),
    );
  }, [desktopSidebarWidth]);

  useEffect(() => {
    window.localStorage.setItem(
      SIDEBAR_COLLAPSED_STORAGE_KEY,
      String(isDesktopSidebarCollapsed),
    );
  }, [isDesktopSidebarCollapsed]);

  useEffect(() => {
    document.body.classList.toggle(
      "chat-sidebar-resizing",
      isDraggingDesktopSidebar,
    );

    return () => {
      document.body.classList.remove("chat-sidebar-resizing");
    };
  }, [isDraggingDesktopSidebar]);

  useEffect(
    () => () => {
      sidebarDragCleanupRef.current?.();
      sidebarDragCleanupRef.current = null;
      document.body.classList.remove("chat-sidebar-resizing");
    },
    [],
  );

  const handleToggleDesktopSidebarCollapsed = useCallback(() => {
    setIsDesktopSidebarCollapsed((previous) => {
      if (previous) {
        setDesktopSidebarWidth(previousDesktopSidebarWidthRef.current);
        return false;
      }

      previousDesktopSidebarWidthRef.current = clampSidebarWidth(
        desktopSidebarWidth,
      );
      return true;
    });
  }, [desktopSidebarWidth]);

  const handleDesktopSidebarResizeStart = useCallback(
    (event: React.PointerEvent<HTMLDivElement>) => {
      if (event.button !== 0 || isDesktopSidebarCollapsed) {
        return;
      }

      event.preventDefault();
      sidebarDragCleanupRef.current?.();
      sidebarDragCleanupRef.current = null;

      const startX = event.clientX;
      const startWidth = clampSidebarWidth(desktopSidebarWidth);
      setIsDraggingDesktopSidebar(true);

      const handlePointerMove = (moveEvent: PointerEvent) => {
        const nextWidth = clampSidebarWidth(
          startWidth + (moveEvent.clientX - startX),
        );
        setDesktopSidebarWidth(nextWidth);
      };

      const handlePointerUp = () => {
        sidebarDragCleanupRef.current?.();
        sidebarDragCleanupRef.current = null;
        setIsDraggingDesktopSidebar(false);
      };

      sidebarDragCleanupRef.current = () => {
        window.removeEventListener("pointermove", handlePointerMove);
        window.removeEventListener("pointerup", handlePointerUp);
        window.removeEventListener("pointercancel", handlePointerUp);
      };

      window.addEventListener("pointermove", handlePointerMove);
      window.addEventListener("pointerup", handlePointerUp);
      window.addEventListener("pointercancel", handlePointerUp);
    },
    [desktopSidebarWidth, isDesktopSidebarCollapsed],
  );

  return (
    <PageTransition>
      <div className="flex h-[calc(100vh-64px)] min-w-0">
        {/* Sidebar */}
        <motion.div
          initial={{ x: -20, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          transition={{ duration: 0.4, ease: "easeOut" }}
          className="h-full shrink-0"
        >
          <ChatSidebar
            selectedChatId={currentChatId}
            onSelectChat={handleSelectChat}
            onNewChat={handleNewChat}
            onDeleteChat={handleDeleteChat}
            desktopSidebarWidth={desktopSidebarWidth}
            isDesktopSidebarCollapsed={isDesktopSidebarCollapsed}
            isDraggingDesktopSidebar={isDraggingDesktopSidebar}
            onToggleDesktopSidebarCollapsed={
              handleToggleDesktopSidebarCollapsed
            }
            onDesktopSidebarResizeStart={handleDesktopSidebarResizeStart}
          />
        </motion.div>

        {/* Main Chat Area */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="flex-1 min-w-0 flex flex-col bg-[var(--color-background)] relative"
        >
          {/* Messages Area */}
          <div className="flex-1 min-w-0 overflow-y-auto overflow-x-hidden p-6 pb-32">
            {isLoadingMessages ? (
              <div className="h-full flex items-center justify-center">
                <LoadingSpinner text={TEXT.chat.loading} />
              </div>
            ) : (
              <div className="mx-auto w-full max-w-4xl min-w-0">
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
                    <StaggerContainer
                      staggerDelay={0.05}
                      initialDelay={0}
                    >
                      {messages.map((message, index) => (
                        <motion.div
                          key={message.messageId}
                          variants={staggerItemVariants}
                        >
                          <ChatMessage
                            message={message}
                            isAI={message.senderId === 0}
                            index={index}
                            stream={
                              message.senderId === 0 &&
                              message.messageId === streamingMessageId
                            }
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
            className="absolute bottom-0 left-0 right-0 min-w-0"
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
