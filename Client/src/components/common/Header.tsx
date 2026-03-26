import React from "react";
import { TEXT } from "../../constants/static";
import { ROUTES } from "../../router";
import HeaderBrand from "./header/HeaderBrand";
import HeaderLogoutButton from "./header/HeaderLogoutButton";
import HeaderNavButton from "./header/HeaderNavButton";
import HeaderThemeToggle from "./header/HeaderThemeToggle";
import { useHeaderController } from "./header/useHeaderController";

/**
 * Header component for the application
 *
 * Responsibility:
 * - Display app branding (centered)
 * - Render authenticated navigation shortcuts
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
    isActivePath,
    goTo,
    logoutAndGoHome,
    toggleTheme,
  } = useHeaderController();

  return (
    <header className="sticky top-0 z-50 bg-[var(--color-surface)] shadow-sm border-b border-[var(--color-border)]" style={{ height: '64px' }}>
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 h-full">
        <div className="relative flex h-full items-center justify-center">
          {/* Left Navigation - 2 buttons (only when authenticated) */}
          {isAuthenticated && (
            <div className="absolute inset-y-0 left-0 flex items-center gap-2">
              <HeaderNavButton
                label="Map"
                isActive={isActivePath(ROUTES.MAP)}
                onClick={() => goTo(ROUTES.MAP)}
              />
              <HeaderNavButton
                label="Chat"
                isActive={isActivePath(ROUTES.CHAT)}
                onClick={() => goTo(ROUTES.CHAT)}
              />
            </div>
          )}

          {/* Center - Logo and uniAI */}
          <HeaderBrand appName={TEXT.header.appName} onHomeClick={() => goTo(ROUTES.HOME)} />

          {/* Right Navigation - 2 buttons + logout + theme toggle */}
          <div className="absolute inset-y-0 right-0 flex items-center gap-2">
            {/* Theme Toggle - Always visible */}
            <HeaderThemeToggle themeName={themeName} onToggle={toggleTheme} />
            
            {/* Settings and About - Only when authenticated */}
            {isAuthenticated && (
              <>
                <HeaderNavButton
                  label="Settings"
                  isActive={isActivePath(ROUTES.SETTINGS)}
                  onClick={() => goTo(ROUTES.SETTINGS)}
                />
                <HeaderNavButton
                  label="About Us"
                  isActive={isActivePath(ROUTES.ABOUT)}
                  onClick={() => goTo(ROUTES.ABOUT)}
                />
                <HeaderLogoutButton onClick={logoutAndGoHome} />
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
