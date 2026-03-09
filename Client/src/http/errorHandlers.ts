import type { AxiosError } from 'axios';
import { Storage } from '../utils/Storage';

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

  if (status === 401) {
    Storage.clearAll();

    const currentPath = window.location.pathname;
    if (
      !currentPath.startsWith('/auth') &&
      !currentPath.startsWith('/signin') &&
      !currentPath.startsWith('/signup')
    ) {
      window.location.href = '/auth';
    }
  }

  if (status === 403) {
    console.error('Access forbidden:', error.response?.data);
  }

  if (status === 404) {
    console.error('Resource not found:', error.response?.data);
  }

  if (status !== undefined && status >= 500) {
    console.error('Server error:', error.response?.data);
  }

  return Promise.reject(error) as never;
}
