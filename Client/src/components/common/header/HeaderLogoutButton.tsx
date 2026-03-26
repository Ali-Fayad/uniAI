/**
 * HeaderLogoutButton
 *
 * Responsibility:
 * - Render the logout icon button.
 *
 * Does NOT:
 * - Perform logout itself
 * - Perform navigation
 */

import React from "react";
import { FiLogOut } from "react-icons/fi";

export interface HeaderLogoutButtonProps {
  onClick: () => void;
}

const HeaderLogoutButton: React.FC<HeaderLogoutButtonProps> = ({ onClick }) => {
  return (
    <button
      onClick={onClick}
      className="p-2 rounded-md text-[var(--color-textSecondary)] hover:text-[var(--color-primary)] transition-colors"
      aria-label="Logout"
      type="button"
    >
      <FiLogOut size={20} />
    </button>
  );
};

export default HeaderLogoutButton;
