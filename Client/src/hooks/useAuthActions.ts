import { useContext } from 'react';
import { AuthActionsContext } from '../context/AuthActionsContext';
import type { AuthActions } from '../context/AuthActionsContext';

/**
 * ISP: use this hook when your component only needs to trigger auth mutations
 * (login, logout, updateUser).
 */
export function useAuthActions(): AuthActions {
  const ctx = useContext(AuthActionsContext);
  if (ctx === undefined) {
    throw new Error('useAuthActions must be used within an AuthProvider');
  }
  return ctx;
}
