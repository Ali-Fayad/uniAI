/**
 * useHeaderController
 *
 * Responsibility:
 * - Own navigation/theme/auth actions needed by the app header.
 * - Provide current route context for active-state rendering.
 *
 * Does NOT:
 * - Render UI
 * - Perform API calls
 */

import { useCallback } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../../hooks/useAuth";
import { useTheme } from "../../../hooks/useTheme";
import { applyThemeByName } from "../../../styles/themes";
import { ROUTES } from "../../../router";

export interface UseHeaderControllerReturn {
  isAuthenticated: boolean;
  themeName: string;
  isActivePath: (path: string) => boolean;
  goTo: (path: string) => void;
  logoutAndGoHome: () => void;
  toggleTheme: () => void;
}

export const useHeaderController = (): UseHeaderControllerReturn => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, logout } = useAuth();
  const { themeName } = useTheme();

  const isActivePath = useCallback((path: string) => location.pathname === path, [location.pathname]);

  const goTo = useCallback((path: string) => navigate(path), [navigate]);

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
    isActivePath,
    goTo,
    logoutAndGoHome,
    toggleTheme,
  };
};
