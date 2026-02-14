/**
 * Application Router Configuration
 * 
 * Centralized routing configuration separated from App.tsx
 * following separation of concerns principles.
 */

import { Routes, Route, Navigate, useLocation } from "react-router-dom";
import { AnimatePresence } from "framer-motion";
import ProtectedRoute from "./components/common/ProtectedRoute";
import PageTransition from "./components/common/PageTransition";

// Page Components
import MainPage from "./components/page/MainPage";
import SettingsPage from "./components/page/SettingsPage";
import ChatPage from "./components/page/ChatPage";
import MapPage from "./components/page/MapPage";

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
 * AnimatePresence enables smooth page transitions.
 */
export const AppRouter = () => {
  const location = useLocation();

  return (
    <AnimatePresence mode="wait">
      <Routes location={location}>
        {/* Main landing page */}
        <Route path="/" element={<PageTransition><MainPage /></PageTransition>} />

        {/* Auth Routes */}
        <Route path="/auth" element={<PageTransition><AuthLanding /></PageTransition>} />
        <Route path="/signin" element={<PageTransition><SignIn /></PageTransition>} />
        <Route path="/signup" element={<PageTransition><SignUp /></PageTransition>} />
        <Route path="/verify" element={<PageTransition><Verify /></PageTransition>} />
        <Route path="/2fa/verify" element={<PageTransition><Verify2FA /></PageTransition>} />
        <Route path="/forgot-password" element={<PageTransition><ForgotPassword /></PageTransition>} />
        <Route path="/forgot-password/confirm" element={<PageTransition><ForgotPasswordConfirm /></PageTransition>} />

        {/* OAuth Callback */}
        <Route path="/google/callback" element={<PageTransition><GoogleCallback /></PageTransition>} />

        {/* Protected Chat Route */}
        <Route
          path="/chat"
          element={
            <PageTransition>
              <ProtectedRoute>
                <ChatPage />
              </ProtectedRoute>
            </PageTransition>
          }
        />

        {/* Settings Page */}
        <Route path="/settings" element={<PageTransition><SettingsPage /></PageTransition>} />

        {/* Map Page */}
        <Route path="/map" element={<PageTransition><MapPage /></PageTransition>} />

        {/* Fallback to main page */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AnimatePresence>
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
  MAP: "/map",
  ABOUT: "/about",
} as const;

export type RouteKey = keyof typeof ROUTES;
export type RoutePath = typeof ROUTES[RouteKey];
