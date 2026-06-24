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
        <div className="flex h-full items-center justify-between gap-4">
          <HeaderBrand appName={TEXT.header.appName} onHomeClick={goHome} />

          <div className="flex items-center gap-2">
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
