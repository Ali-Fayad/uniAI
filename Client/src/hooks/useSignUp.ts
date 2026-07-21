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
import { isStrongPassword, isValidEmail, isValidUsername } from '../lib/validation';
import { useNotification } from './useNotification';

export type SignUpField = 'username' | 'firstName' | 'lastName' | 'email' | 'password' | 'confirmPassword';
export type SignUpFieldErrors = Partial<Record<SignUpField, string>>;

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
  usernameCheckStatus: EmailCheckStatus;
  emailAvailabilityMessage: string;
  emailCheckStatus: EmailCheckStatus;
  fieldErrors: SignUpFieldErrors;
  visibleErrors: SignUpFieldErrors;
  passwordRules: Array<{ label: string; satisfied: boolean }>;
  markFieldTouched: (field: SignUpField) => void;
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
  const { showNotification } = useNotification();

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
  const [touched, setTouched] = useState<Partial<Record<SignUpField, boolean>>>({});
  const [hasSubmitted, setHasSubmitted] = useState(false);

  const {
    status: usernameCheckStatus,
    message: usernameAvailabilityMessage,
  } = useUsernameCheck(username, 400);

  const {
    status: emailCheckStatus,
    message: emailAvailabilityMessage,
  } = useEmailCheck(email, 400);

  const passwordRules = useMemo(() => [
    { label: 'At least 8 characters', satisfied: password.length >= 8 },
    { label: 'At most 100 characters', satisfied: password.length <= 100 && password.length > 0 },
    { label: 'One uppercase letter', satisfied: /[A-Z]/.test(password) },
    { label: 'One lowercase letter', satisfied: /[a-z]/.test(password) },
    { label: 'One number', satisfied: /\d/.test(password) },
    { label: 'One special character', satisfied: /[^A-Za-z0-9]/.test(password) },
    { label: 'No spaces', satisfied: password.length > 0 && !/\s/.test(password) },
  ], [password]);

  const fieldErrors = useMemo<SignUpFieldErrors>(() => {
    const next: SignUpFieldErrors = {};
    const normalizedUsername = username.trim();
    const normalizedEmail = email.trim();

    if (!normalizedUsername) next.username = 'Username is required.';
    else if (!isValidUsername(normalizedUsername)) next.username = 'Use 2–50 letters, numbers, or underscores.';
    else if (usernameCheckStatus === 'unavailable') next.username = 'This username is already in use.';
    else if (usernameCheckStatus === 'error') next.username = 'Username availability could not be checked.';
    else if (usernameCheckStatus === 'checking' || usernameCheckStatus === 'idle') next.username = 'Username availability is still being checked.';

    if (firstName.trim().length < 2) next.firstName = 'First name must contain at least 2 characters.';
    if (lastName.trim().length < 2) next.lastName = 'Last name must contain at least 2 characters.';

    if (!normalizedEmail) next.email = 'Email is required.';
    else if (!isValidEmail(normalizedEmail)) next.email = 'Please enter a valid email address.';
    else if (emailCheckStatus === 'unavailable') next.email = 'This email is already registered.';
    else if (emailCheckStatus === 'error') next.email = 'Email availability could not be checked.';
    else if (emailCheckStatus === 'checking' || emailCheckStatus === 'idle') next.email = 'Email availability is still being checked.';

    if (!password) next.password = 'Password is required.';
    else if (!isStrongPassword(password)) next.password = 'Password does not meet all requirements.';
    if (!confirmPassword) next.confirmPassword = 'Please confirm your password.';
    else if (password !== confirmPassword) next.confirmPassword = 'Passwords do not match.';
    return next;
  }, [
    username, firstName, lastName, email, password, confirmPassword,
    usernameCheckStatus, emailCheckStatus,
  ]);

  const visibleErrors = useMemo(() => {
    const next: SignUpFieldErrors = {};
    (Object.keys(fieldErrors) as SignUpField[]).forEach((field) => {
      if (hasSubmitted || touched[field]) next[field] = fieldErrors[field];
    });
    return next;
  }, [fieldErrors, hasSubmitted, touched]);

  const markFieldTouched = (field: SignUpField) => {
    setTouched((current) => ({ ...current, [field]: true }));
  };

  const focusFirstInvalid = (errors: SignUpFieldErrors) => {
    const first = (['username', 'firstName', 'lastName', 'email', 'password', 'confirmPassword'] as SignUpField[])
      .find((field) => errors[field]);
    if (!first) return;
    window.requestAnimationFrame(() => {
      const element = document.getElementById(`signup-${first}`);
      element?.focus();
      element?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    });
  };

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setHasSubmitted(true);
    setTouched({ username: true, firstName: true, lastName: true, email: true, password: true, confirmPassword: true });

    if (Object.keys(fieldErrors).length > 0) {
      setError('Please correct the highlighted fields before continuing.');
      focusFirstInvalid(fieldErrors);
      return;
    }

    const normalizedEmail = email.trim().toLowerCase();
    const normalizedUsername = username.trim();
    setIsLoading(true);

    try {
      const data: SignUpDto = {
        username: normalizedUsername,
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: normalizedEmail,
        password,
      };
      const response = await authService.signUp(data);

      if (response?.token) {
        const payload = decodeJwt(response.token);
        const isVerified = payload?.isVerified === true;

        if (isVerified) {
          showNotification({ type: 'success', message: 'Account created successfully.' });
          login(response.token);
          navigate(ROUTES.CHAT);
          return;
        }

        showNotification({ type: 'success', message: "Account created successfully. We've sent a verification code to your email." });
        navigate(ROUTES.VERIFY, { state: { email: normalizedEmail } });
        return;
      }

      showNotification({ type: 'success', message: "Account created successfully. We've sent a verification code to your email." });
      navigate(ROUTES.VERIFY, { state: { email: normalizedEmail } });
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
    usernameCheckStatus,
    emailAvailabilityMessage,
    emailCheckStatus,
    fieldErrors,
    visibleErrors,
    passwordRules,
    markFieldTouched,
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
