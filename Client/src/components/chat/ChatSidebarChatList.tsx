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
  isCollapsed: boolean;
  onNewChat: () => void;
  onSelectChat: (chatId: number) => void;
  onDeleteChat: (chatId: number, event: React.MouseEvent) => void;
  onToggleCollapse: () => void;
}

const ChatSidebarChatList: React.FC<ChatSidebarChatListProps> = ({
  chats,
  isLoading,
  selectedChatId,
  isCollapsed,
  onNewChat,
  onSelectChat,
  onDeleteChat,
  onToggleCollapse,
}) => {
  const getChatInitial = (title: string | null | undefined) => {
    const value = title?.trim();
    return value ? (value[0]?.toUpperCase() ?? "C") : "C";
  };

  const handleChatKeyDown = (
    event: React.KeyboardEvent<HTMLDivElement>,
    chatId: number,
  ) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      onSelectChat(chatId);
    }
  };

  return (
    <>
      <div
        className={`flex items-center gap-3 border-b border-[var(--color-border)] ${
          isCollapsed
            ? "justify-between px-6 py-5 lg:justify-center lg:px-3 lg:py-4"
            : "justify-between px-6 py-5"
        }`}
      >
        <h2
          className={`text-xl font-bold text-[var(--color-textPrimary)] ${
            isCollapsed ? "block lg:hidden" : "block"
          }`}
        >
          Chats
        </h2>

        <div
          className={`flex items-center ${
            isCollapsed ? "gap-2 lg:gap-1" : "gap-2"
          }`}
        >
          <button
            onClick={onToggleCollapse}
            className="hidden items-center justify-center rounded-full p-2 text-[var(--color-textSecondary)] transition-colors hover:bg-[var(--color-elevatedSurface)] lg:inline-flex"
            title={isCollapsed ? "Expand sidebar" : "Collapse sidebar"}
            aria-label={isCollapsed ? "Expand sidebar" : "Collapse sidebar"}
            aria-expanded={!isCollapsed}
            type="button"
          >
            <span className="material-symbols-outlined text-[20px]">
              {isCollapsed ? "chevron_right" : "chevron_left"}
            </span>
          </button>

          <button
            onClick={onNewChat}
            className="rounded-full p-2 text-[var(--color-primary)] transition-colors hover:bg-[var(--color-elevatedSurface)]"
            title="New Chat"
            aria-label="New Chat"
            type="button"
          >
            <span className="material-symbols-outlined">add_circle</span>
          </button>
        </div>
      </div>

      <div
        className={`flex-1 overflow-y-auto px-4 py-4 scrollbar-thin scrollbar-thumb-[var(--color-border)] ${
          isCollapsed ? "space-y-2 lg:px-2 lg:py-3" : "space-y-2"
        }`}
      >
        {isLoading ? (
          <div className="flex h-20 items-center justify-center text-[var(--color-textSecondary)]">
            <span className="text-sm">Loading...</span>
          </div>
        ) : chats.length === 0 ? (
          <div
            className={`px-1 text-center text-sm text-[var(--color-textSecondary)] ${
              isCollapsed ? "py-8 lg:py-6" : "py-8"
            }`}
          >
            <span className={isCollapsed ? "lg:hidden" : ""}>
              No chats yet
            </span>

            {isCollapsed && (
              <span
                className="hidden material-symbols-outlined lg:inline"
                aria-label="No chats yet"
                title="No chats yet"
              >
                chat_bubble_outline
              </span>
            )}
          </div>
        ) : (
          chats.map((chat) => {
            const isSelected = selectedChatId === chat.id;
            const title = chat.title || "New Chat";

            return (
              <div
                key={chat.id}
                onClick={() => onSelectChat(chat.id)}
                onKeyDown={(event) => handleChatKeyDown(event, chat.id)}
                className={[
                  "group relative mb-1 flex w-full cursor-pointer items-center rounded-2xl transition-all duration-200",
                  isCollapsed
                    ? "justify-between px-4 py-3 lg:justify-center lg:px-2"
                    : "justify-between px-4 py-3",
                  isSelected
                    ? "bg-[var(--color-primary)] font-medium text-[var(--color-background)] shadow-md"
                    : "bg-[var(--color-elevatedSurface)] text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]",
                ].join(" ")}
                title={title}
                aria-label={title}
                role="button"
                tabIndex={0}
              >
                <div
                  className={`flex min-w-0 flex-1 ${
                    isCollapsed ? "pr-8 lg:flex-none lg:pr-0" : "pr-8"
                  }`}
                >
                  {isCollapsed && (
                    <div className="hidden items-center justify-center lg:flex">
                      <div
                        className={[
                          "flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-semibold",
                          isSelected
                            ? "bg-[rgba(255,255,255,0.18)] text-[var(--color-background)]"
                            : "bg-[var(--color-background)]/10 text-[var(--color-textPrimary)]",
                        ].join(" ")}
                      >
                        {getChatInitial(chat.title)}
                      </div>
                    </div>
                  )}

                  <div
                    className={`min-w-0 ${
                      isCollapsed ? "block lg:hidden" : "block"
                    }`}
                  >
                    <p className="truncate text-sm">{title}</p>
                    <p className="mt-0.5 text-[10px] opacity-60">
                      {new Date(chat.updatedAt).toLocaleDateString()}
                    </p>
                  </div>
                </div>

                <button
                  onClick={(event) => onDeleteChat(chat.id, event)}
                  className={`absolute right-3 top-1/2 -translate-y-1/2 rounded-lg p-1.5 text-red-400 opacity-100 transition-all hover:bg-red-50 lg:opacity-0 lg:group-hover:opacity-100 ${
                    isCollapsed ? "lg:hidden" : ""
                  }`}
                  type="button"
                  aria-label={`Delete ${title}`}
                >
                  <span className="material-symbols-outlined text-[18px]">
                    delete
                  </span>
                </button>
              </div>
            );
          })
        )}
      </div>
    </>
  );
};

export default ChatSidebarChatList;