import { Routes, Route, Navigate } from 'react-router-dom';
import DefaultView from './pages/DefaultView';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp';
import ForgotPassword from './pages/ForgotPassword';
import VerificationCode from './pages/VerificationCode';
import OAuthLoading from './pages/OAuthLoading';
import MainPage from './pages/MainPage';

const App = () => {
  return (
    <Routes>
      {/* Main marketing page */}
      <Route path="/" element={<MainPage />} />

      {/* Auth Routes */}
      <Route path="/auth" element={<DefaultView />} />
      <Route path="/auth/signin" element={<SignIn />} />
      <Route path="/auth/signup" element={<SignUp />} />
      <Route path="/auth/forgot-password" element={<ForgotPassword />} />
      <Route path="/auth/verification" element={<VerificationCode />} />

      {/* OAuth Routes */}
      <Route path="/auth/google" element={<OAuthLoading provider="Google" />} />
      <Route path="/auth/github" element={<OAuthLoading provider="GitHub" />} />

      {/* keep a fallback to /auth for unknown routes */}
      <Route path="*" element={<Navigate to="/auth" replace />} />
    </Routes>
  );
};

export default App;
