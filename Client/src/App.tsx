import { Routes, Route, Navigate, useLocation } from "react-router-dom";
import { AnimatePresence } from "framer-motion";
import PageTransition from "./components/common/PageTransition";
import { AuthProvider } from "./context/AuthContext";
import Header from "./components/common/Header";
import ProtectedRoute from "./components/common/ProtectedRoute";
import MainPage from "./components/page/MainPage";
import SettingsPage from "./components/page/SettingsPage";
import AuthLanding from "./components/page/auth/AuthLanding";
import SignIn from "./components/page/auth/SignIn";
import SignUp from "./components/page/auth/SignUp";
import Verify from "./components/page/auth/Verify";
import Verify2FA from "./components/page/auth/Verify2FA";
import ForgotPassword from "./components/page/auth/ForgotPassword";
import ForgotPasswordConfirm from "./components/page/auth/ForgotPasswordConfirm";
import GoogleCallback from "./components/page/auth/GoogleCallback";
import ChatPage from "./components/page/ChatPage";

const App = () => {
  const location = useLocation();
  const showHeader = location.pathname !== "/chat";

  return (
    <AuthProvider>
      <div className="min-h-screen flex flex-col bg-custom-light">
        {showHeader && <Header />}

        <div className="flex-grow">
          <AnimatePresence mode="wait">
            <Routes location={location} key={location.pathname}>
              {/* Main landing page */}
              <Route
                path="/"
                element={
                  <PageTransition>
                    <MainPage />
                  </PageTransition>
                }
              />

              {/* Auth Routes */}
              <Route
                path="/auth"
                element={
                  <PageTransition>
                    <AuthLanding />
                  </PageTransition>
                }
              />
              <Route
                path="/signin"
                element={
                  <PageTransition>
                    <SignIn />
                  </PageTransition>
                }
              />
              <Route
                path="/signup"
                element={
                  <PageTransition>
                    <SignUp />
                  </PageTransition>
                }
              />
              <Route
                path="/verify"
                element={
                  <PageTransition>
                    <Verify />
                  </PageTransition>
                }
              />
              <Route
                path="/2fa/verify"
                element={
                  <PageTransition>
                    <Verify2FA />
                  </PageTransition>
                }
              />
              <Route
                path="/forgot-password"
                element={
                  <PageTransition>
                    <ForgotPassword />
                  </PageTransition>
                }
              />
              <Route
                path="/forgot-password/confirm"
                element={
                  <PageTransition>
                    <ForgotPasswordConfirm />
                  </PageTransition>
                }
              />

              {/* OAuth Callback */}
              <Route
                path="/google/callback"
                element={
                  <PageTransition>
                    <GoogleCallback />
                  </PageTransition>
                }
              />

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
              <Route
                path="/settings"
                element={
                  <PageTransition>
                    <SettingsPage />
                  </PageTransition>
                }
              />

              {/* Fallback to main page */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </AnimatePresence>
        </div>
      </div>
    </AuthProvider>
  );
};

export default App;
