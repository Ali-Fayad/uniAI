/**
 * ChatSidebarMobileToggle
 *
 * Responsibility:
 * - Render the mobile-only button that toggles the sidebar open/closed.
 *
 * Does NOT:
 * - Fetch chats
 * - Handle navigation
 * - Perform API calls
 */

import React from "react";

export interface ChatSidebarMobileToggleProps {
  isOpen: boolean;
  onToggle: () => void;
}

const ChatSidebarMobileToggle: React.FC<ChatSidebarMobileToggleProps> = ({
  isOpen,
  onToggle,
}) => {
  return (
    <button
      onClick={onToggle}
      className="lg:hidden fixed top-4 left-4 z-50 bg-[var(--color-primary)] text-[var(--color-background)] p-2 rounded-full shadow-lg hover:bg-[var(--color-primaryVariant)] transition-colors"
      aria-label="Toggle Sidebar"
    >
      <span className="material-symbols-outlined">{isOpen ? "close" : "menu"}</span>
    </button>
  );
};

export default ChatSidebarMobileToggle;
