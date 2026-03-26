/**
 * ChatSidebarProfileMenu
 *
 * Responsibility:
 * - Render the user profile section and dropdown actions (Settings, Logout).
 *
 * Does NOT:
 * - Fetch user data
 * - Perform navigation side effects directly
 * - Perform API calls
 */

import React from "react";

export interface ChatSidebarProfileMenuProps {
  user: { firstName?: string | null; lastName?: string | null; username?: string | null } | null;
  isOpen: boolean;
  menuRef: React.RefObject<HTMLDivElement | null>;
  onToggle: () => void;
  onNavigateSettings: () => void;
  onLogout: () => void;
}

const ChatSidebarProfileMenu: React.FC<ChatSidebarProfileMenuProps> = ({
  user,
  isOpen,
  menuRef,
  onToggle,
  onNavigateSettings,
  onLogout,
}) => {
  const initials = user?.firstName?.[0] || user?.username?.[0] || "U";

  return (
    <div className="p-4 border-t border-[var(--color-border)] bg-[var(--color-surface)]">
      <div className="relative" ref={menuRef}>
        <button
          onClick={onToggle}
          className="flex items-center gap-3 p-2 rounded-xl bg-[var(--color-elevatedSurface)] hover:bg-[var(--color-elevatedSurface)] transition-colors w-full"
          aria-expanded={isOpen}
          type="button"
        >
          <div className="w-10 h-10 rounded-full bg-[var(--color-primary)] flex items-center justify-center text-[var(--color-background)] font-bold">
            {initials}
          </div>
          <div className="flex-1 min-w-0 text-left">
            <p className="text-sm font-semibold text-[var(--color-textPrimary)] truncate">
              {user?.firstName} {user?.lastName}
            </p>
            <p className="text-xs text-[var(--color-textSecondary)] truncate">@{user?.username}</p>
          </div>
          <span className="material-symbols-outlined text-[var(--color-textSecondary)]">more_vert</span>
        </button>

        {isOpen && (
          <div className="absolute left-4 right-4 bottom-14 z-40">
            <div className="bg-[var(--color-surface)] rounded-md shadow-lg ring-1 ring-[var(--color-border)]">
              <ul className="py-1">
                <li>
                  <button
                    onClick={onNavigateSettings}
                    className="w-full text-left px-4 py-2 text-sm text-[var(--color-textPrimary)] hover:bg-[var(--color-elevatedSurface)]"
                    type="button"
                  >
                    Settings
                  </button>
                </li>
                <li>
                  <button
                    onClick={onLogout}
                    className="w-full text-left px-4 py-2 text-sm text-[var(--color-error)] hover:bg-[var(--color-elevatedSurface)]"
                    type="button"
                  >
                    Logout
                  </button>
                </li>
              </ul>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatSidebarProfileMenu;
