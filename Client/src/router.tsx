/**
 * Application Router Configuration
 * 
 * Centralized routing configuration separated from App.tsx
 * following separation of concerns principles.
 */

import { Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from "./components/common/ProtectedRoute";

// Page Components
import MainPage from "./components/page/MainPage";
import SettingsPage from "./components/page/SettingsPage";
import ChatPage from "./components/page/ChatPage";

// Auth Pages
import AuthLanding from "./components/page/auth/AuthLanding";
import SignIn from "./components/page/auth/SignIn";
import SignUp from "./components/page/auth/SignUp";
import Verify from "./components/page/auth/Verify";
import Verify2FA from "./components/page/auth/Verify2FA";
import ForgotPassword from "./components/page/auth/ForgotPassword";
import ForgotPasswordConfirm from "./components/page/auth/ForgotPasswordConfirm";
import GoogleCallback from "./components/page/auth/GoogleCallback";

/**
 * Application Router Component
 * 
 * Defines all application routes in a centralized location.
 * Protected routes use the ProtectedRoute wrapper component.
 */
export const AppRouter = () => {
  return (
    <Routes>
      {/* Main landing page */}
      <Route path="/" element={<MainPage />} />

      {/* Auth Routes */}
      <Route path="/auth" element={<AuthLanding />} />
      <Route path="/signin" element={<SignIn />} />
      <Route path="/signup" element={<SignUp />} />
      <Route path="/verify" element={<Verify />} />
      <Route path="/2fa/verify" element={<Verify2FA />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/forgot-password/confirm" element={<ForgotPasswordConfirm />} />

      {/* OAuth Callback */}
      <Route path="/google/callback" element={<GoogleCallback />} />

      {/* Protected Chat Route */}
      <Route
        path="/chat"
        element={
          <ProtectedRoute>
            <ChatPage />
          </ProtectedRoute>
        }
      />

      {/* Settings Page */}
      <Route path="/settings" element={<SettingsPage />} />

      {/* Fallback to main page */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

/**
 * Route Path Constants
 * 
 * Centralized route paths for type-safe navigation.
 * Use these constants instead of hardcoded strings in navigate() calls.
 */
export const ROUTES = {
  HOME: "/",
  AUTH: "/auth",
  SIGN_IN: "/signin",
  SIGN_UP: "/signup",
  VERIFY: "/verify",
  VERIFY_2FA: "/2fa/verify",
  FORGOT_PASSWORD: "/forgot-password",
  FORGOT_PASSWORD_CONFIRM: "/forgot-password/confirm",
  GOOGLE_CALLBACK: "/google/callback",
  CHAT: "/chat",
  SETTINGS: "/settings",
  ABOUT: "/about",
} as const;

export type RouteKey = keyof typeof ROUTES;
export type RoutePath = typeof ROUTES[RouteKey];
