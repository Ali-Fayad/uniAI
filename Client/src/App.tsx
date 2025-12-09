import { Routes, Route, Navigate } from 'react-router-dom';
import DefaultView from './pages/DefaultView';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp';
import ForgotPassword from './pages/ForgotPassword';
import VerificationCode from './pages/VerificationCode';
import OAuthLoading from './pages/OAuthLoading';
import MainPage from './pages/MainPage';
import ChatPage from './pages/ChatPage';
import { ProtectedRoute } from './components/ProtectedRoute';
import { PublicRoute } from './components/PublicRoute';

const App = () => {
  return (
    <Routes>
      {/* Main marketing page */}
      <Route path="/" element={<MainPage />} />

      {/* Protected Chat Route */}
      <Route
        path="/chat"
        element={
          <ProtectedRoute>
            <ChatPage />
          </ProtectedRoute>
        }
      />

      {/* Auth Routes - redirect to /chat if already authenticated */}
      <Route
        path="/auth"
        element={
          <PublicRoute>
            <DefaultView />
          </PublicRoute>
        }
      />
      <Route
        path="/auth/signin"
        element={
          <PublicRoute>
            <SignIn />
          </PublicRoute>
        }
      />
      <Route
        path="/auth/signup"
        element={
          <PublicRoute>
            <SignUp />
          </PublicRoute>
        }
      />
      <Route
        path="/auth/forgot-password"
        element={
          <PublicRoute>
            <ForgotPassword />
          </PublicRoute>
        }
      />
      <Route
        path="/auth/verification"
        element={
          <PublicRoute>
            <VerificationCode />
          </PublicRoute>
        }
      />

      {/* OAuth Routes */}
      <Route path="/auth/google" element={<OAuthLoading provider="Google" />} />
      <Route path="/auth/github" element={<OAuthLoading provider="GitHub" />} />

      {/* Fallback to /auth for unknown routes */}
      <Route path="*" element={<Navigate to="/auth" replace />} />
    </Routes>
  );
};

export default App;
