import type { AxiosError, AxiosResponse } from 'axios';
import { Storage } from './Storage';

/**
 * OCP: add new HTTP error handling behaviour by providing additional
 * ErrorHandler entries — existing handlers are never modified.
 *
 * SRP: this module's only reason to change is HTTP error handling policy.
 */
export interface ErrorHandler {
  /** Exact status code or a predicate function. */
  statusCode: number | ((status: number) => boolean);
  handle(response: AxiosResponse): void;
}

const AUTH_PATHS = ['/auth', '/signin', '/signup'];

/**
 * Default set of error handlers.
 * Pass a custom array to `handleHttpError` to extend or override behaviour
 * without modifying this file (OCP).
 */
export const defaultErrorHandlers: ErrorHandler[] = [
  {
    // 401 – clear session and redirect to auth unless already there
    statusCode: 401,
    handle() {
      Storage.clearAll();
      const currentPath = window.location.pathname;
      const alreadyOnAuth = AUTH_PATHS.some((p) => currentPath.startsWith(p));
      if (!alreadyOnAuth) {
        window.location.href = '/auth';
      }
    },
  },
  {
    statusCode: 403,
    handle(response) {
      console.error('Access forbidden:', response.data);
    },
  },
  {
    statusCode: 404,
    handle(response) {
      console.error('Resource not found:', response.data);
    },
  },
  {
    // 5xx – server-side errors
    statusCode: (status) => status >= 500,
    handle(response) {
      console.error('Server error:', response.data);
    },
  },
];

/**
 * Walk `handlers` in order and invoke the first match for the given error's
 * HTTP status.  If no handler matches the error is silently passed through so
 * the calling interceptor can still reject the promise.
 */
export function handleHttpError(
  error: AxiosError,
  handlers: ErrorHandler[] = defaultErrorHandlers,
): void {
  if (!error.response) return;
  const status = error.response.status;

  for (const handler of handlers) {
    const matches =
      typeof handler.statusCode === 'function'
        ? handler.statusCode(status)
        : handler.statusCode === status;

    if (matches) {
      handler.handle(error.response as AxiosResponse);
      return;
    }
  }
}
