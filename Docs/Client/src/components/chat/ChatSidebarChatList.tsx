/**
 * ChatSidebarChatList
 *
 * Responsibility:
 * - Render the sidebar header and chat list UI.
 *
 * Does NOT:
 * - Fetch chats
 * - Delete chats via API
 * - Own sidebar open/close state
 */

import React from "react";
import type { Chat } from "../../types/dto";

export interface ChatSidebarChatListProps {
  chats: Chat[];
  isLoading: boolean;
  selectedChatId: number | null;
  onNewChat: () => void;
  onSelectChat: (chatId: number) => void;
  onDeleteChat: (chatId: number, e: React.MouseEvent) => void;
}

const ChatSidebarChatList: React.FC<ChatSidebarChatListProps> = ({
  chats,
  isLoading,
  selectedChatId,
  onNewChat,
  onSelectChat,
  onDeleteChat,
}) => {
  return (
    <>
      <div className="p-6 flex items-center justify-between">
        <h2 className="text-xl font-bold text-[var(--color-textPrimary)]">Chats</h2>
        <button
          onClick={onNewChat}
          className="p-2 rounded-full hover:bg-[var(--color-elevatedSurface)] transition-colors text-[var(--color-primary)]"
          title="New Chat"
          type="button"
        >
          <span className="material-symbols-outlined">add_circle</span>
        </button>
      </div>

      <div className="flex-1 overflow-y-auto px-4 space-y-2 scrollbar-thin scrollbar-thumb-[var(--color-border)]">
        {isLoading ? (
          <div className="flex items-center justify-center h-20 text-[var(--color-textSecondary)]">
            <span className="text-sm">Loading...</span>
          </div>
        ) : chats.length === 0 ? (
          <div className="text-center text-[var(--color-textSecondary)] py-8 text-sm">
            No chats yet
          </div>
        ) : (
          chats.map((chat) => (
            <div
              key={chat.id}
              onClick={() => onSelectChat(chat.id)}
              className={
                "group relative flex items-center justify-between px-4 py-3 rounded-2xl cursor-pointer transition-all duration-200 mb-1 " +
                (selectedChatId === chat.id
                  ? "bg-[var(--color-primary)] text-[var(--color-background)] font-medium shadow-md"
                  : "bg-[var(--color-elevatedSurface)] hover:bg-[var(--color-elevatedSurface)] text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]")
              }
            >
              <div className="flex-1 min-w-0 pr-8">
                <p className="truncate text-sm">{chat.title || "New Chat"}</p>
                <p className="text-[10px] opacity-60 mt-0.5">
                  {new Date(chat.updatedAt).toLocaleDateString()}
                </p>
              </div>

              <button
                onClick={(e) => onDeleteChat(chat.id, e)}
                className="absolute right-3 top-1/2 -translate-y-1/2 p-1.5 rounded-lg opacity-0 group-hover:opacity-100 hover:bg-red-50 text-red-400 transition-all"
                type="button"
                aria-label="Delete chat"
              >
                <span className="material-symbols-outlined text-[18px]">delete</span>
              </button>
            </div>
          ))
        )}
      </div>
    </>
  );
};

export default ChatSidebarChatList;
