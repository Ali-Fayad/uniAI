import { useEffect, useMemo, useState } from 'react';
import { authService } from '../services/auth';

export type EmailCheckStatus =
  | 'idle'
  | 'invalid'
  | 'checking'
  | 'available'
  | 'unavailable'
  | 'error';

interface UseEmailCheckResult {
  status: EmailCheckStatus;
  message: string;
  normalizedEmail: string;
  isFormatValid: boolean;
}

/**
 * Debounced email availability check used by auth forms.
 */
export const useEmailCheck = (email: string, debounceMs = 400): UseEmailCheckResult => {
  const normalizedEmail = useMemo(() => email.trim().toLowerCase(), [email]);

  const isFormatValid = useMemo(() => {
    if (!normalizedEmail) {
      return false;
    }
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail);
  }, [normalizedEmail]);

  const [status, setStatus] = useState<EmailCheckStatus>('idle');
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!normalizedEmail) {
      setStatus('idle');
      setMessage('');
      return;
    }

    if (!isFormatValid) {
      setStatus('invalid');
      setMessage('Invalid email format');
      return;
    }

    setStatus('checking');
    setMessage('Checking availability...');

    const timeout = setTimeout(async () => {
      try {
        const response = await authService.checkEmailAvailability(normalizedEmail);
        if (response.available) {
          setStatus('available');
          setMessage(response.message || 'Email is available');
        } else {
          setStatus('unavailable');
          setMessage(response.message || 'Email already registered');
        }
      } catch {
        setStatus('error');
        setMessage('Unable to validate email right now');
      }
    }, debounceMs);

    return () => clearTimeout(timeout);
  }, [debounceMs, isFormatValid, normalizedEmail]);

  return {
    status,
    message,
    normalizedEmail,
    isFormatValid,
  };
};