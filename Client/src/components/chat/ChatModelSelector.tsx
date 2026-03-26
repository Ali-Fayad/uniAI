/**
 * ChatModelSelector
 *
 * Responsibility:
 * - Render the model selector button and dropdown menu.
 * - Own open/close UI state for the dropdown.
 *
 * Does NOT:
 * - Send messages
 * - Fetch models from an API
 * - Persist model selection
 */

import React, { useCallback, useRef, useState } from "react";
import { useOnClickOutside } from "../../hooks/useOnClickOutside";

const ChatModelSelector: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  const closeMenu = useCallback(() => setIsOpen(false), []);
  useOnClickOutside(menuRef, closeMenu, {
    eventType: "mousedown",
    enabled: isOpen,
  });

  return (
    <div className="relative" ref={menuRef}>
      <button
        type="button"
        onClick={() => setIsOpen((prev) => !prev)}
        className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-[var(--color-surface)] hover:bg-[var(--color-surfaceHover)] text-xs font-medium text-[var(--color-textSecondary)] transition-colors border border-[var(--color-border)]"
        aria-haspopup="menu"
        aria-expanded={isOpen}
      >
        <span className="material-symbols-outlined text-[16px] text-[var(--color-primary)]">
          smart_toy
        </span>
        <span>uniAI Basic</span>
        <span className="material-symbols-outlined text-[14px]">expand_more</span>
      </button>

      {isOpen && (
        <div className="absolute bottom-full left-0 mb-2 w-48 bg-[var(--color-surface)] rounded-xl shadow-xl border border-[var(--color-border)] overflow-hidden animate-fadeIn z-10">
          <div className="p-1" role="menu" aria-label="Model selection">
            <div className="px-3 py-2 bg-[var(--color-surfaceHover)] rounded-lg mb-1" role="menuitem">
              <p className="text-xs font-semibold text-[var(--color-textPrimary)]">uniAI Basic</p>
              <p className="text-[10px] text-[var(--color-textSecondary)]">Fast & Efficient</p>
            </div>
            <div className="px-3 py-2 opacity-50 cursor-not-allowed" aria-disabled="true">
              <p className="text-xs font-semibold text-[var(--color-textPrimary)]">GPT-4o</p>
              <p className="text-[10px] text-[var(--color-primary)]">Coming soon</p>
            </div>
            <div className="px-3 py-2 opacity-50 cursor-not-allowed" aria-disabled="true">
              <p className="text-xs font-semibold text-[var(--color-textPrimary)]">Claude 3.5</p>
              <p className="text-[10px] text-[var(--color-primary)]">Coming soon</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatModelSelector;
