/**
 * useSignIn hook
 *
 * Owns the sign-in form state, validation, API call, and post-login
 * navigation.  Extracted from SignIn.tsx so the component is responsible
 * only for rendering (SRP).  The component no longer depends on authService
 * or the auth context directly (DIP – it depends on this hook's interface).
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/auth';
import { useAuth } from './useAuth';
import { TEXT } from '../constants/static';
import { ROUTES } from '../router';
import type { SignInDto } from '../types/dto';

export interface UseSignInReturn {
  email: string;
  password: string;
  isLoading: boolean;
  error: string;
  showPassword: boolean;
  setEmail: (v: string) => void;
  setPassword: (v: string) => void;
  setShowPassword: (v: boolean) => void;
  handleLogin: (e: React.FormEvent) => Promise<void>;
}

export const useSignIn = (): UseSignInReturn => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const data: SignInDto = { email, password };
      const response = await authService.signIn(data);

      if (!response?.token) {
        throw new Error('Sign in failed: missing token');
      }

      login(response.token);
      navigate(ROUTES.CHAT);
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as {
          response?: { status?: number; data?: { message?: string } };
        };

        if (axiosError.response?.status === 401) {
          navigate(ROUTES.VERIFY_2FA, { state: { email } });
          return;
        }

        if (axiosError.response?.status === 202) {
          navigate(ROUTES.VERIFY, { state: { email } });
          return;
        }

        setError(
          axiosError.response?.data?.message ??
            TEXT.auth.signIn.errors.invalidCredentials,
        );
      } else {
        setError(TEXT.common.error);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return {
    email,
    password,
    isLoading,
    error,
    showPassword,
    setEmail,
    setPassword,
    setShowPassword,
    handleLogin,
  };
};
