import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Header from './components/common/Header';
import ProtectedRoute from './components/common/ProtectedRoute';
import MainPage from './components/page/MainPage';
import SignIn from './components/page/auth/SignIn';
import SignUp from './components/page/auth/SignUp';
import Verify from './components/page/auth/Verify';
import Verify2FA from './components/page/auth/Verify2FA';
import ForgotPassword from './components/page/auth/ForgotPassword';
import ForgotPasswordConfirm from './components/page/auth/ForgotPasswordConfirm';
import GoogleCallback from './components/page/auth/GoogleCallback';
import ChatPage from './components/page/ChatPage';

const App = () => {
  const location = useLocation();
  const showHeader = location.pathname !== '/chat';

  return (
    <AuthProvider>
      <div className="min-h-screen flex flex-col bg-custom-light">
        {showHeader && <Header />}
        
        <div className="flex-grow">
          <Routes>
            {/* Main landing page */}
            <Route path="/" element={<MainPage />} />

            {/* Auth Routes - redirect /auth to /signin for simplicity */}
            <Route path="/auth" element={<Navigate to="/signin" replace />} />
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

            {/* Fallback to main page */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </div>
    </AuthProvider>
  );
};

export default App;
