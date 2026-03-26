/**
 * ChatSidebarBackdrop
 *
 * Responsibility:
 * - Render the semi-transparent backdrop behind the sidebar on mobile.
 *
 * Does NOT:
 * - Manage sidebar state
 * - Perform API calls
 */

import React from "react";

export interface ChatSidebarBackdropProps {
  isVisible: boolean;
  onClick: () => void;
}

const ChatSidebarBackdrop: React.FC<ChatSidebarBackdropProps> = ({
  isVisible,
  onClick,
}) => {
  if (!isVisible) {
    return null;
  }

  return (
    <div
      className="fixed inset-0 bg-black/40 z-30 lg:hidden"
      onClick={onClick}
    />
  );
};

export default ChatSidebarBackdrop;
