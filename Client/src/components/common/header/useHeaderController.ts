/**
 * useHeaderController
 *
 * Responsibility:
 * - Own branding, theme, and auth actions needed by the app header.
 *
 * Does NOT:
 * - Render UI
 * - Perform API calls
 */

import { useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../hooks/useAuth";
import { useTheme } from "../../../hooks/useTheme";
import { applyThemeByName } from "../../../styles/themes";
import { ROUTES } from "../../../router";

export interface UseHeaderControllerReturn {
  isAuthenticated: boolean;
  themeName: string;
  goHome: () => void;
  logoutAndGoHome: () => void;
  toggleTheme: () => void;
}

export const useHeaderController = (): UseHeaderControllerReturn => {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();
  const { themeName } = useTheme();

  const goHome = useCallback(() => navigate(ROUTES.HOME), [navigate]);

  const logoutAndGoHome = useCallback(() => {
    logout();
    navigate(ROUTES.HOME);
  }, [logout, navigate]);

  const toggleTheme = useCallback(() => {
    const newTheme = themeName === "light" ? "dark" : "light";
    applyThemeByName(newTheme);
  }, [themeName]);

  return {
    isAuthenticated,
    themeName,
    goHome,
    logoutAndGoHome,
    toggleTheme,
  };
};
