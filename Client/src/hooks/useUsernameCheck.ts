import { useEffect, useMemo, useState } from 'react';
import { authService } from '../services/auth';
import type { EmailCheckStatus } from './useEmailCheck';
import { isValidUsername } from '../lib/validation';

interface UseUsernameCheckResult {
  status: EmailCheckStatus;
  message: string;
  normalizedUsername: string;
  isFormatValid: boolean;
}

/**
 * Debounced username availability check used by signup forms.
 */
export const useUsernameCheck = (username: string, debounceMs = 400): UseUsernameCheckResult => {
  const normalizedUsername = useMemo(() => username.trim().toLowerCase(), [username]);

  const isFormatValid = useMemo(() => isValidUsername(normalizedUsername), [normalizedUsername]);

  const [status, setStatus] = useState<EmailCheckStatus>('idle');
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!normalizedUsername) {
      setStatus('idle');
      setMessage('');
      return;
    }

    if (!isFormatValid) {
      setStatus('invalid');
      setMessage('Use 2–50 letters, numbers, or underscores');
      return;
    }

    setStatus('checking');
    setMessage('Checking availability...');

    const timeout = setTimeout(async () => {
      try {
        const response = await authService.checkUsernameAvailability(normalizedUsername);
        if (response.available) {
          setStatus('available');
          setMessage(response.message || 'Username is available');
        } else {
          setStatus('unavailable');
          setMessage(response.message || 'Username already in use');
        }
      } catch {
        setStatus('error');
        setMessage('Unable to validate username right now');
      }
    }, debounceMs);

    return () => clearTimeout(timeout);
  }, [debounceMs, isFormatValid, normalizedUsername]);

  return {
    status,
    message,
    normalizedUsername,
    isFormatValid,
  };
};
