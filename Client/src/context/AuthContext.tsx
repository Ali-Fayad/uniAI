import { createContext, useState, useEffect, useMemo } from 'react';
import type { ReactNode } from 'react';
import { Storage } from '../utils/Storage';
import { extractUserFromToken } from '../utils/JwtDecode';
import type { UserData } from '../types/user';
import { AuthStateContext } from './AuthStateContext';
import type { AuthState } from './AuthStateContext';
import { AuthActionsContext } from './AuthActionsContext';
import type { AuthActions } from './AuthActionsContext';

/**
 * Combined type kept for backward compatibility with existing `useAuth()` consumers.
 * New code should prefer `useAuthState()` or `useAuthActions()` (ISP).
 */
export type AuthContextType = AuthState & AuthActions;

/** @deprecated Use AuthStateContext / AuthActionsContext directly. */
export const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * SRP: AuthProvider is responsible only for providing auth state & actions to the
 * component tree via two focused contexts.  Storage I/O and JWT decoding are
 * delegated to their own modules.
 */
export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<UserData | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialise auth state from storage on mount (single responsibility: bootstrap)
  useEffect(() => {
    const token = Storage.getToken();
    const storedUser = Storage.getUser();

    if (token && storedUser) {
      setUser(storedUser);
    } else if (token) {
      const extractedUser = extractUserFromToken(token);
      if (extractedUser) {
        setUser(extractedUser);
        Storage.setUser(extractedUser);
      } else {
        Storage.clearAll();
      }
    }

    setIsLoading(false);
  }, []);

  // ----- actions (stable references via useMemo) -----
  const actions: AuthActions = useMemo(() => ({
    login(token: string, userData?: UserData) {
      Storage.setToken(token);
      const userToStore = userData ?? extractUserFromToken(token);
      if (userToStore) {
        Storage.setUser(userToStore);
        setUser(userToStore);
      }
    },
    logout() {
      Storage.clearAll();
      setUser(null);
    },
    updateUser(userData: UserData) {
      Storage.setUser(userData);
      setUser(userData);
    },
  }), []);

  // ----- state (recalculated only when user/isLoading change) -----
  const state: AuthState = useMemo(() => ({
    user,
    isAuthenticated: !!user,
    isLoading,
  }), [user, isLoading]);

  // Combined value kept for backward-compatible AuthContext consumers
  const combined: AuthContextType = useMemo(() => ({ ...state, ...actions }), [state, actions]);

  return (
    <AuthStateContext.Provider value={state}>
      <AuthActionsContext.Provider value={actions}>
        {/* backward-compat wrapper */}
        <AuthContext.Provider value={combined}>
          {children}
        </AuthContext.Provider>
      </AuthActionsContext.Provider>
    </AuthStateContext.Provider>
  );
};
