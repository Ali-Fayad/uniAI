import { useAuthState } from './useAuthState';
import { useAuthActions } from './useAuthActions';

/**
 * Convenience hook that combines useAuthState + useAuthActions.
 * Existing consumers keep working without changes.
 *
 * Prefer the focused hooks for new code (ISP):
 *   - useAuthState()   – read-only (user, isAuthenticated, isLoading)
 *   - useAuthActions() – mutations  (login, logout, updateUser)
 */
export const useAuth = () => {
  const state = useAuthState();
  const actions = useAuthActions();
  return { ...state, ...actions };
};
