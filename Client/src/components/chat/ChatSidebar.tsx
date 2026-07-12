import React from "react";
import { useChatSidebar } from "../../hooks/useChatSidebar";
import ChatSidebarBackdrop from "./ChatSidebarBackdrop";
import ChatSidebarChatList from "./ChatSidebarChatList";
import ChatSidebarMobileToggle from "./ChatSidebarMobileToggle";
import ChatSidebarProfileMenu from "./ChatSidebarProfileMenu";

interface ChatSidebarProps {
  selectedChatId: number | null;
  onSelectChat: (chatId: number) => void;
  onNewChat: () => void;
  onDeleteChat: (chatId: number) => void;
  desktopSidebarWidth: number;
  isDesktopSidebarCollapsed: boolean;
  isDraggingDesktopSidebar: boolean;
  onToggleDesktopSidebarCollapsed: () => void;
  onDesktopSidebarResizeStart: (
    event: React.PointerEvent<HTMLDivElement>,
  ) => void;
}

/**
 * ChatSidebar
 *
 * Responsible for rendering the chat sidebar UI (chat list, mobile toggle,
 * and profile menu) and delegating state/business logic to `useChatSidebar`.
 *
 * Does NOT perform API calls directly.
 */

const ChatSidebar: React.FC<ChatSidebarProps> = ({
  selectedChatId,
  onSelectChat,
  onNewChat,
  onDeleteChat,
  desktopSidebarWidth,
  isDesktopSidebarCollapsed,
  isDraggingDesktopSidebar,
  onToggleDesktopSidebarCollapsed,
  onDesktopSidebarResizeStart,
}) => {
  const {
    user,
    chats,
    isLoading,
    isSidebarOpen,
    profileMenuOpen,
    profileMenuRef,
    setIsSidebarOpen,
    setProfileMenuOpen,
    handleDeleteChat,
    handleLogout,
    handleNavigateSettings,
  } = useChatSidebar(selectedChatId, onDeleteChat);

  return (
    <>
      <ChatSidebarMobileToggle
        isOpen={isSidebarOpen}
        onToggle={() => setIsSidebarOpen(!isSidebarOpen)}
      />

      <ChatSidebarBackdrop
        isVisible={isSidebarOpen}
        onClick={() => setIsSidebarOpen(false)}
      />

      {/* Sidebar Container */}
      <aside
        style={
          {
            "--chat-sidebar-expanded-width": `${desktopSidebarWidth}px`,
            "--chat-sidebar-collapsed-width": "64px",
            "--chat-sidebar-current-width": isDesktopSidebarCollapsed
              ? "64px"
              : `${desktopSidebarWidth}px`,
          } as React.CSSProperties
        }
        data-collapsed={isDesktopSidebarCollapsed}
        data-dragging={isDraggingDesktopSidebar}
        className={`
          chat-sidebar-shell
          fixed inset-y-0 left-0 z-40 w-80 bg-[var(--color-surface)] border-r border-[var(--color-border)]
          transform transition-transform duration-300 ease-in-out lg:relative lg:translate-x-0
          lg:w-[var(--chat-sidebar-current-width)] lg:min-w-[var(--chat-sidebar-current-width)]
          ${isSidebarOpen ? "translate-x-0" : "-translate-x-full"}
          flex flex-col h-full shadow-sm lg:shadow-none
        `}
      >
        <ChatSidebarChatList
          chats={chats}
          isLoading={isLoading}
          selectedChatId={selectedChatId}
          isCollapsed={isDesktopSidebarCollapsed}
          onNewChat={() => {
            onNewChat();
            setIsSidebarOpen(false);
          }}
          onSelectChat={(chatId) => {
            onSelectChat(chatId);
            setIsSidebarOpen(false);
          }}
          onDeleteChat={(chatId, e) => void handleDeleteChat(chatId, e)}
          onToggleCollapse={onToggleDesktopSidebarCollapsed}
        />

        <ChatSidebarProfileMenu
          user={user ?? null}
          isOpen={profileMenuOpen}
          menuRef={profileMenuRef}
          isCollapsed={isDesktopSidebarCollapsed}
          onToggle={() => setProfileMenuOpen((s) => !s)}
          onNavigateSettings={handleNavigateSettings}
          onLogout={handleLogout}
        />

        <div
          className="chat-sidebar-resize-handle hidden lg:block"
          role="separator"
          aria-orientation="vertical"
          aria-label="Resize sidebar"
          onPointerDown={onDesktopSidebarResizeStart}
        />
      </aside>
    </>
  );
};

export default ChatSidebar;
