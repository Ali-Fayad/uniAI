/**
 * useSignUp hook
 *
 * Owns the sign-up form state, password-match validation, API call, and
 * post-registration navigation.  Extracted from SignUp.tsx so the component
 * is responsible only for rendering (SRP).
 */

import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/auth';
import { useAuth } from './useAuth';
import { TEXT } from '../constants/static';
import { ROUTES } from '../router';
import type { SignUpDto } from '../types/dto';

export interface UseSignUpReturn {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
  isLoading: boolean;
  error: string;
  emailAvailabilityMessage: string;
  isEmailAvailable: boolean;
  isEmailChecking: boolean;
  canSubmit: boolean;
  showPassword: boolean;
  showConfirmPassword: boolean;
  setUsername: (v: string) => void;
  setFirstName: (v: string) => void;
  setLastName: (v: string) => void;
  setEmail: (v: string) => void;
  setPassword: (v: string) => void;
  setConfirmPassword: (v: string) => void;
  setShowPassword: (v: boolean) => void;
  setShowConfirmPassword: (v: boolean) => void;
  handleSignup: (e: React.FormEvent) => Promise<void>;
}

export const useSignUp = (): UseSignUpReturn => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [username, setUsername] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [emailAvailabilityMessage, setEmailAvailabilityMessage] = useState('');
  const [isEmailAvailable, setIsEmailAvailable] = useState(false);
  const [isEmailChecking, setIsEmailChecking] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const normalizedEmail = useMemo(() => email.trim().toLowerCase(), [email]);

  const isEmailFormatValid = useMemo(() => {
    if (!normalizedEmail) {
      return false;
    }
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail);
  }, [normalizedEmail]);

  useEffect(() => {
    if (!normalizedEmail) {
      setEmailAvailabilityMessage('');
      setIsEmailAvailable(false);
      setIsEmailChecking(false);
      return;
    }

    if (!isEmailFormatValid) {
      setEmailAvailabilityMessage('Please enter a valid email address');
      setIsEmailAvailable(false);
      setIsEmailChecking(false);
      return;
    }

    setIsEmailChecking(true);

    const timeout = setTimeout(async () => {
      try {
        const response = await authService.checkEmailAvailability(normalizedEmail);
        setIsEmailAvailable(response.available);
        setEmailAvailabilityMessage(response.message);
      } catch {
        setIsEmailAvailable(false);
        setEmailAvailabilityMessage('Unable to validate email right now');
      } finally {
        setIsEmailChecking(false);
      }
    }, 350);

    return () => clearTimeout(timeout);
  }, [normalizedEmail, isEmailFormatValid]);

  const canSubmit = useMemo(() => {
    return !isLoading && !isEmailChecking && isEmailFormatValid && isEmailAvailable;
  }, [isLoading, isEmailChecking, isEmailFormatValid, isEmailAvailable]);

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError(TEXT.auth.signUp.errors.passwordMismatch);
      return;
    }

    if (!isEmailAvailable) {
      setError('Email already in use');
      return;
    }

    setIsLoading(true);

    try {
      const data: SignUpDto = { username, firstName, lastName, email, password };
      const response = await authService.signUp(data);

      if (response?.token) {
        login(response.token);
        navigate(ROUTES.CHAT);
        return;
      }

      navigate(ROUTES.VERIFY, { state: { email } });
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as {
          response?: { status?: number; data?: { message?: string } };
        };

        if (axiosError.response?.status === 202) {
          navigate(ROUTES.VERIFY, { state: { email } });
          return;
        }

        setError(
          axiosError.response?.data?.message ??
            TEXT.auth.signUp.errors.signUpFailed,
        );
      } else {
        setError(TEXT.common.error);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return {
    username,
    firstName,
    lastName,
    email,
    password,
    confirmPassword,
    isLoading,
    error,
    emailAvailabilityMessage,
    isEmailAvailable,
    isEmailChecking,
    canSubmit,
    showPassword,
    showConfirmPassword,
    setUsername,
    setFirstName,
    setLastName,
    setEmail,
    setPassword,
    setConfirmPassword,
    setShowPassword,
    setShowConfirmPassword,
    handleSignup,
  };
};
