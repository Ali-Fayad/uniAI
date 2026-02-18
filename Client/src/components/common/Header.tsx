import React from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { FiLogOut, FiSun, FiMoon } from "react-icons/fi";
import { useAuth } from "../../hooks/useAuth";
import { useTheme } from "../../hooks/useTheme";
import { applyThemeByName } from "../../styles/themes";
import { TEXT } from "../../constants/static";
import { ROUTES } from "../../router";

/**
 * Header component for the application
 * 
 * Responsibilities:
 * - Display app branding (centered)
 * - Navigation controls (2 left, 2 right) - only when authenticated
 * - Logout functionality
 * - Theme toggle (always visible)
 */
const Header: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, logout } = useAuth();
  const { themeName } = useTheme();

  const handleLogout = () => {
    logout();
    navigate(ROUTES.HOME);
  };

  const toggleTheme = () => {
    const newTheme = themeName === "light" ? "dark" : "light";
    applyThemeByName(newTheme);
  };

  const navButtonClass = (path: string) => 
    `px-4 py-2 text-sm font-medium transition-colors rounded-md ${
      location.pathname === path
        ? "text-[var(--color-primary)] bg-[var(--color-primary)]/10"
        : "text-[var(--color-textSecondary)] hover:text-[var(--color-primary)]"
    }`;

  return (
    <header className="sticky top-0 z-50 bg-[var(--color-surface)] shadow-sm border-b border-[var(--color-border)]" style={{ height: '64px' }}>
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 h-full">
        <div className="relative flex h-full items-center justify-center">
          {/* Left Navigation - 2 buttons (only when authenticated) */}
          {isAuthenticated && (
            <div className="absolute inset-y-0 left-0 flex items-center gap-2">
              <button
                onClick={() => navigate(ROUTES.MAP)}
                className={navButtonClass(ROUTES.MAP)}
              >
                Map
              </button>
              <button
                onClick={() => navigate(ROUTES.CHAT)}
                className={navButtonClass(ROUTES.CHAT)}
              >
                Chat
              </button>
            </div>
          )}

          {/* Center - Logo and uniAI */}
          <div className="flex items-center gap-3">
            <svg
              className="h-8 w-auto text-[var(--color-primary)]"
              fill="currentColor"
              viewBox="0 0 54 44"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path d="M26.5816 43.3134L53.1633 0H39.8724L26.5816 26.5816L13.2908 0H0L26.5816 43.3134Z"></path>
            </svg>
            <button
              onClick={() => navigate(ROUTES.HOME)}
              className="font-playful text-2xl font-bold tracking-tight text-[var(--color-textPrimary)] hover:text-[var(--color-primary)] transition-colors cursor-pointer bg-transparent"
            >
              {TEXT.header.appName}
            </button>
          </div>

          {/* Right Navigation - 2 buttons + logout + theme toggle */}
          <div className="absolute inset-y-0 right-0 flex items-center gap-2">
            {/* Theme Toggle - Always visible */}
            <button
              onClick={toggleTheme}
              className="p-2 rounded-md text-[var(--color-textSecondary)] hover:text-[var(--color-primary)] transition-colors"
              aria-label={`Switch to ${themeName === "light" ? "dark" : "light"} mode`}
            >
              {themeName === "light" ? <FiMoon size={20} /> : <FiSun size={20} />}
            </button>
            
            {/* Settings and About - Only when authenticated */}
            {isAuthenticated && (
              <>
                <button
                  onClick={() => navigate(ROUTES.SETTINGS)}
                  className={navButtonClass(ROUTES.SETTINGS)}
                >
                  Settings
                </button>
                <button
                  onClick={() => navigate(ROUTES.ABOUT)}
                  className={navButtonClass(ROUTES.ABOUT)}
                >
                  About Us
                </button>
                <button
                  onClick={handleLogout}
                  className="p-2 rounded-md text-[var(--color-textSecondary)] hover:text-[var(--color-primary)] transition-colors"
                  aria-label="Logout"
                >
                  <FiLogOut size={20} />
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
