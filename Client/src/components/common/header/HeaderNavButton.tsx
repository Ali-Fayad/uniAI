/**
 * HeaderNavButton
 *
 * Responsibility:
 * - Render a single header navigation button with an active state.
 *
 * Does NOT:
 * - Perform routing directly
 * - Read location/auth state
 */

import React from "react";

export interface HeaderNavButtonProps {
  label: string;
  isActive: boolean;
  onClick: () => void;
}

const HeaderNavButton: React.FC<HeaderNavButtonProps> = ({
  label,
  isActive,
  onClick,
}) => {
  const className =
    "px-4 py-2 text-sm font-medium transition-colors rounded-md " +
    (isActive
      ? "text-[var(--color-primary)] bg-[var(--color-primary)]/10"
      : "text-[var(--color-textSecondary)] hover:text-[var(--color-primary)]");

  return (
    <button onClick={onClick} className={className} type="button">
      {label}
    </button>
  );
};

export default HeaderNavButton;
