import React from "react";
import { TEXT } from "../../constants/static";
import HeaderBrand from "./header/HeaderBrand";
import HeaderLogoutButton from "./header/HeaderLogoutButton";
import HeaderThemeToggle from "./header/HeaderThemeToggle";
import { useHeaderController } from "./header/useHeaderController";

/**
 * Header component for the application
 *
 * Responsibility:
 * - Display app branding (centered)
 * - Provide theme toggle and logout affordances
 *
 * Does NOT:
 * - Fetch user/profile data
 * - Perform API calls
 */
const Header: React.FC = () => {
  const {
    isAuthenticated,
    themeName,
    goHome,
    logoutAndGoHome,
    toggleTheme,
  } = useHeaderController();

  return (
    <header className="sticky top-0 z-50 bg-[var(--color-surface)] shadow-sm border-b border-[var(--color-border)] print:hidden" style={{ height: '64px' }}>
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 h-full">
        <div className="grid h-full grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-4">
          <div aria-hidden="true" />

          <div className="justify-self-center">
            <HeaderBrand appName={TEXT.header.appName} onHomeClick={goHome} />
          </div>

          <div className="flex items-center justify-end gap-2">
            <HeaderThemeToggle themeName={themeName} onToggle={toggleTheme} />
            {isAuthenticated && (
              <HeaderLogoutButton onClick={logoutAndGoHome} />
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
