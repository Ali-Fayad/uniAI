/**
 * HeaderThemeToggle
 *
 * Responsibility:
 * - Render the theme toggle button and icon.
 *
 * Does NOT:
 * - Apply theme directly
 * - Perform navigation
 */

import React from "react";
import { FiMoon, FiSun } from "react-icons/fi";

export interface HeaderThemeToggleProps {
  themeName: string;
  onToggle: () => void;
}

const HeaderThemeToggle: React.FC<HeaderThemeToggleProps> = ({
  themeName,
  onToggle,
}) => {
  const next = themeName === "light" ? "dark" : "light";

  return (
    <button
      onClick={onToggle}
      className="p-2 rounded-md text-[var(--color-textSecondary)] hover:text-[var(--color-primary)] transition-colors"
      aria-label={`Switch to ${next} mode`}
      type="button"
    >
      {themeName === "light" ? <FiMoon size={20} /> : <FiSun size={20} />}
    </button>
  );
};

export default HeaderThemeToggle;
