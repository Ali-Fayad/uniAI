/**
 * HeaderBrand
 *
 * Responsibility:
 * - Render the header brand/logo and app name link.
 *
 * Does NOT:
 * - Read auth/theme state
 * - Perform API calls
 */

import React from "react";

export interface HeaderBrandProps {
  appName: string;
  onHomeClick: () => void;
}

const HeaderBrand: React.FC<HeaderBrandProps> = ({ appName, onHomeClick }) => {
  return (
    <div className="flex items-center gap-3">
      <svg
        className="h-8 w-auto text-[var(--color-primary)]"
        fill="currentColor"
        viewBox="0 0 54 44"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
      >
        <path d="M26.5816 43.3134L53.1633 0H39.8724L26.5816 26.5816L13.2908 0H0L26.5816 43.3134Z"></path>
      </svg>
      <button
        onClick={onHomeClick}
        className="font-playful text-2xl font-bold tracking-tight text-[var(--color-textPrimary)] hover:text-[var(--color-primary)] transition-colors cursor-pointer bg-transparent"
        type="button"
      >
        {appName}
      </button>
    </div>
  );
};

export default HeaderBrand;
