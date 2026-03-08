import { createContext } from 'react';
import type { UserData } from '../types/user';

/**
 * ISP: read-only auth state — consumed by components that only need
 * to know who is logged in (e.g. Header, ProtectedRoute).
 */
export interface AuthState {
  user: UserData | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export const AuthStateContext = createContext<AuthState | undefined>(undefined);
