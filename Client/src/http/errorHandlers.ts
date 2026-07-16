import type { AxiosError } from 'axios';
import { Storage } from '../utils/Storage';
import { requestNavigation } from '../events/navigationEvents';

type ProfileIncompleteHandling = 'local' | 'navigate';

type ProfileAwareRequestConfig = {
  profileIncompleteHandling?: ProfileIncompleteHandling;
};

/**
 * handleResponseError
 *
 * Single-responsibility module that translates HTTP error responses into
 * appropriate side-effects (clear storage, redirect) without touching
 * the core Axios-client configuration.
 *
 * Extracted from api.ts to satisfy SRP: api.ts configures the transport
 * layer while this module owns error-response policy.
 */
export function handleResponseError(error: AxiosError): never {
  const status = error.response?.status;
  const bodyMessage =
    typeof error.response?.data === 'string'
      ? error.response?.data
      : (error.response?.data as { message?: string } | undefined)?.message;
  const isAuthRequiredMessage = bodyMessage === 'Authentication required';

  if (status === 401) {
    Storage.clearAll();

    const currentPath = window.location.pathname;
    if (
      !currentPath.startsWith('/auth') &&
      !currentPath.startsWith('/signin') &&
      !currentPath.startsWith('/signup')
    ) {
      requestNavigation({ path: '/auth', reason: 'auth-required', clearAuth: true });
    }
  }

  if (status === 403) {
    console.error('Access forbidden:', error.response?.data);

    if (isAuthRequiredMessage) {
      Storage.clearAll();
      const currentPath = window.location.pathname;
      if (!currentPath.startsWith('/auth') && !currentPath.startsWith('/signin') && !currentPath.startsWith('/signup')) {
        requestNavigation({ path: '/auth', reason: 'auth-required', clearAuth: true });
      }
    }
  }

  if (status === 410) {
    const profileHandling = (error.config as ProfileAwareRequestConfig | undefined)?.profileIncompleteHandling;

    if (profileHandling !== 'local') {
      console.error('Profile incomplete:', error.response?.data);
      const currentPath = window.location.pathname;
      if (!currentPath.startsWith('/personal-info')) {
        requestNavigation({
          path: `/personal-info?returnTo=${encodeURIComponent(currentPath)}`,
          reason: 'profile-incomplete',
        });
      }
    }
  }

  if (status === 404) {
    console.error('Resource not found:', error.response?.data);
  }

  if (status !== undefined && status >= 500) {
    console.error('Server error:', error.response?.data);
  }

  return Promise.reject(error) as never;
}
