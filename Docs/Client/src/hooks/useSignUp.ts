/**
 * useSignUp hook
 *
 * Owns the sign-up form state, password-match validation, API call, and
 * post-registration navigation.  Extracted from SignUp.tsx so the component
 * is responsible only for rendering (SRP).
 */

import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/auth';
import { useAuth } from './useAuth';
import { useEmailCheck } from './useEmailCheck';
import { useUsernameCheck } from './useUsernameCheck';
import { TEXT } from '../constants/static';
import { ROUTES } from '../router';
import type { SignUpDto } from '../types/dto';
import type { EmailCheckStatus } from './useEmailCheck';
import { decodeJwt } from '../utils/JwtDecode';

export interface UseSignUpReturn {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
  isLoading: boolean;
  error: string;
  usernameAvailabilityMessage: string;
  isUsernameAvailable: boolean;
  isUsernameChecking: boolean;
  usernameCheckStatus: EmailCheckStatus;
  emailAvailabilityMessage: string;
  isEmailAvailable: boolean;
  isEmailChecking: boolean;
  emailCheckStatus: EmailCheckStatus;
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
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const {
    status: usernameCheckStatus,
    message: usernameAvailabilityMessage,
    isFormatValid: isUsernameFormatValid,
  } = useUsernameCheck(username, 400);

  const {
    status: emailCheckStatus,
    message: emailAvailabilityMessage,
    isFormatValid: isEmailFormatValid,
  } = useEmailCheck(email, 400);

  const isUsernameChecking = usernameCheckStatus === 'checking';
  const isUsernameAvailable = usernameCheckStatus === 'available';
  const isEmailChecking = emailCheckStatus === 'checking';
  const isEmailAvailable = emailCheckStatus === 'available';

  const canSubmit = useMemo(() => {
    return (
      !isLoading &&
      !isUsernameChecking &&
      !isEmailChecking &&
      isUsernameFormatValid &&
      isEmailFormatValid &&
      isUsernameAvailable &&
      isEmailAvailable
    );
  }, [
    isLoading,
    isUsernameChecking,
    isEmailChecking,
    isUsernameFormatValid,
    isEmailFormatValid,
    isUsernameAvailable,
    isEmailAvailable,
  ]);

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError(TEXT.auth.signUp.errors.passwordMismatch);
      return;
    }

    if (!isUsernameAvailable) {
      setError('Username already in use');
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
        const payload = decodeJwt(response.token);
        const isVerified = payload?.isVerified === true;

        if (isVerified) {
          login(response.token);
          navigate(ROUTES.CHAT);
          return;
        }

        navigate(ROUTES.VERIFY, { state: { email } });
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
    usernameAvailabilityMessage,
    isUsernameAvailable,
    isUsernameChecking,
    usernameCheckStatus,
    emailAvailabilityMessage,
    isEmailAvailable,
    isEmailChecking,
    emailCheckStatus,
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
