/**
 * ChatSidebarProfileMenu
 *
 * Responsibility:
 * - Render the user profile section and dropdown actions.
 *
 * Does NOT:
 * - Fetch user data
 * - Perform navigation side effects directly
 * - Perform API calls
 */

import React from "react";

export interface ChatSidebarProfileMenuProps {
  user: {
    firstName?: string | null;
    lastName?: string | null;
    username?: string | null;
  } | null;
  isOpen: boolean;
  menuRef: React.RefObject<HTMLDivElement | null>;
  isCollapsed: boolean;
  onToggle: () => void;
  onNavigateSettings: () => void;
  onLogout: () => void;
}

const ChatSidebarProfileMenu: React.FC<ChatSidebarProfileMenuProps> = ({
  user,
  isOpen,
  menuRef,
  isCollapsed,
  onToggle,
  onNavigateSettings,
  onLogout,
}) => {
  const initials = user?.firstName?.[0] || user?.username?.[0] || "U";

  return (
    <div
      className={`border-t border-[var(--color-border)] bg-[var(--color-surface)] ${
        isCollapsed ? "p-4 lg:p-2" : "p-4"
      }`}
    >
      <div className="relative" ref={menuRef}>
        <button
          onClick={onToggle}
          className={`flex w-full items-center rounded-xl bg-[var(--color-elevatedSurface)] transition-colors hover:bg-[var(--color-elevatedSurface)] ${
            isCollapsed
              ? "justify-between gap-3 p-2 lg:justify-center lg:gap-0"
              : "gap-3 p-2"
          }`}
          aria-expanded={isOpen}
          aria-haspopup="menu"
          aria-label="Profile menu"
          type="button"
        >
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[var(--color-primary)] font-bold text-[var(--color-background)]">
            {initials}
          </div>

          <div
            className={`min-w-0 flex-1 text-left ${
              isCollapsed ? "block lg:hidden" : "block"
            }`}
          >
            <p className="truncate text-sm font-semibold text-[var(--color-textPrimary)]">
              {user?.firstName} {user?.lastName}
            </p>
            <p className="truncate text-xs text-[var(--color-textSecondary)]">
              @{user?.username}
            </p>
          </div>

          <span
            className={`material-symbols-outlined text-[var(--color-textSecondary)] ${
              isCollapsed ? "block lg:hidden" : "block"
            }`}
          >
            more_vert
          </span>
        </button>

        {isOpen && (
          <div
            className={`absolute bottom-14 z-40 ${
              isCollapsed
                ? "left-4 right-4 lg:left-14 lg:right-auto lg:min-w-[11rem]"
                : "left-4 right-4"
            }`}
          >
            <div className="rounded-md bg-[var(--color-surface)] shadow-lg ring-1 ring-[var(--color-border)]">
              <ul className="py-1" role="menu">
                <li>
                  <button
                    onClick={onNavigateSettings}
                    className="w-full px-4 py-2 text-left text-sm text-[var(--color-textPrimary)] hover:bg-[var(--color-elevatedSurface)]"
                    type="button"
                    role="menuitem"
                  >
                    Settings
                  </button>
                </li>

                <li>
                  <button
                    onClick={onLogout}
                    className="w-full px-4 py-2 text-left text-sm text-[var(--color-error)] hover:bg-[var(--color-elevatedSurface)]"
                    type="button"
                    role="menuitem"
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