import { useContext } from 'react';
import { AuthStateContext } from '../context/AuthStateContext';
import type { AuthState } from '../context/AuthStateContext';

/**
 * ISP: use this hook when your component only needs read access to auth state
 * (user, isAuthenticated, isLoading).
 */
export function useAuthState(): AuthState {
  const ctx = useContext(AuthStateContext);
  if (ctx === undefined) {
    throw new Error('useAuthState must be used within an AuthProvider');
  }
  return ctx;
}
