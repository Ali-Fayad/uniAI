import { Routes, Route, Navigate } from 'react-router-dom';
import DefaultView from './pages/DefaultView';
import SignIn from './pages/Signin';
import SignUp from './pages/SignUp';
import ForgotPassword from './pages/ForgotPassword';
import VerificationCode from './pages/VerificationCode';
import OAuthLoading from './pages/OAuthLoading';

const App = () => {
  return (
    <Routes>
      {/* Redirect root to /auth */}
      <Route path="/" element={<Navigate to="/auth" replace />} />
      
      {/* Auth Routes */}
      <Route path="/auth" element={<DefaultView />} />
      <Route path="/auth/signin" element={<SignIn />} />
      <Route path="/auth/signup" element={<SignUp />} />
      <Route path="/auth/forgot-password" element={<ForgotPassword />} />
      <Route path="/auth/verification" element={<VerificationCode />} />
      
      {/* OAuth Routes */}
      <Route path="/auth/google" element={<OAuthLoading provider="Google" />} />
      <Route path="/auth/github" element={<OAuthLoading provider="GitHub" />} />
    </Routes>
  );
};

export default App;