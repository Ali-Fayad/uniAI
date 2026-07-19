/**
 * useSignIn hook
 *
 * Owns the sign-in form state, validation, API call, and post-login
 * navigation. Extracted from SignIn.tsx so the component is responsible
 * only for rendering.
 */

import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { authService } from '../services/auth';
import { useAuth } from './useAuth';
import { TEXT } from '../constants/static';
import { ROUTES } from '../router';
import { decodeJwt } from '../utils/JwtDecode';
import type { SignInDto } from '../types/dto';

export interface UseSignInReturn {
  email: string;
  password: string;
  isLoading: boolean;
  error: string;
  showPassword: boolean;
  setEmail: (value: string) => void;
  setPassword: (value: string) => void;
  setShowPassword: (value: boolean) => void;
  handleLogin: (event: React.FormEvent) => Promise<void>;
}

export const useSignIn = (): UseSignInReturn => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const returnTo = new URLSearchParams(location.search).get('returnTo');

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const handleLogin = async (event: React.FormEvent) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const data: SignInDto = {
        email,
        password,
      };

      const response = await authService.signIn(data);

      if (!response?.token) {
        throw new Error('Sign in failed: missing token');
      }

      console.log('[SIGN_IN_RESPONSE]', response);
      console.log('[SIGN_IN_TOKEN_PAYLOAD]', decodeJwt(response.token));

      console.log('[SIGN_IN] Calling login');
      login(response.token);

      // Temporary diagnostic delay.
      // This must not remain as the final production implementation.
      window.setTimeout(() => {
        console.log('[SIGN_IN] Navigating after diagnostic delay');
        navigate(returnTo || ROUTES.CHAT, { replace: true });
      }, 1000);
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as {
          response?: {
            status?: number;
            data?: {
              message?: string;
            };
          };
        };

        const responseData = axiosError.response?.data;
        const responseMessage =
          typeof responseData === 'string'
            ? responseData
            : responseData?.message ?? '';
        const isTwoFactorResponse = /two[- ]factor|2fa/i.test(responseMessage);

        if (
          axiosError.response?.status === 401 ||
          (axiosError.response?.status === 403 && isTwoFactorResponse)
        ) {
          navigate(ROUTES.VERIFY_2FA, {
            state: { email },
          });
          return;
        }

        if (axiosError.response?.status === 202) {
          navigate(ROUTES.VERIFY, {
            state: { email },
          });
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
