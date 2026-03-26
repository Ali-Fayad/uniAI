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
        className={`
          fixed inset-y-0 left-0 z-40 w-80 bg-[var(--color-surface)] border-r border-[var(--color-border)]
          transform transition-transform duration-300 ease-in-out lg:relative lg:translate-x-0
          ${isSidebarOpen ? "translate-x-0" : "-translate-x-full"}
          flex flex-col h-full shadow-sm
        `}
      >
        <ChatSidebarChatList
          chats={chats}
          isLoading={isLoading}
          selectedChatId={selectedChatId}
          onNewChat={() => {
            onNewChat();
            setIsSidebarOpen(false);
          }}
          onSelectChat={(chatId) => {
            onSelectChat(chatId);
            setIsSidebarOpen(false);
          }}
          onDeleteChat={(chatId, e) => void handleDeleteChat(chatId, e)}
        />

        <ChatSidebarProfileMenu
          user={user ?? null}
          isOpen={profileMenuOpen}
          menuRef={profileMenuRef}
          onToggle={() => setProfileMenuOpen((s) => !s)}
          onNavigateSettings={handleNavigateSettings}
          onLogout={handleLogout}
        />
      </aside>
    </>
  );
};

export default ChatSidebar;
