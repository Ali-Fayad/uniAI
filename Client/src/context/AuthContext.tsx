/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useEffect, useCallback } from 'react';
import type { ReactNode, Dispatch, SetStateAction } from 'react';
import { useNavigate } from 'react-router-dom';
import { Storage } from '../utils/Storage';
import { extractUserFromToken } from '../utils/JwtDecode';
import type { UserData } from '../types/dto';
import { NAVIGATION_REQUEST_EVENT } from '../events/navigationEvents';
import type { NavigationRequest } from '../events/navigationEvents';
import { userService } from '../services/user';

interface AuthContextType {
  user: UserData | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (token: string, userData?: UserData) => void;
  logout: () => void;
  updateUser: (userData: UserData) => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

const normalizeAuthenticatedUser = (userData: UserData): UserData => ({
  ...userData,
  isVerified: userData.isVerified !== false,
  twoFactorEnabled: userData.twoFactorEnabled ?? userData.isTwoFacAuth ?? false,
  isTwoFacAuth: userData.isTwoFacAuth ?? userData.twoFactorEnabled ?? false,
});

const restoreAuthenticatedUser = (
  userData: UserData | null,
  setUser: Dispatch<SetStateAction<UserData | null>>,
) => {
  if (!userData) {
    Storage.removeUser();
    setUser(null);
    return null;
  }

  const normalizedUser = normalizeAuthenticatedUser(userData);
  Storage.setUser(normalizedUser);
  setUser(normalizedUser);
  return normalizedUser;
};

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const navigate = useNavigate();
  const [user, setUser] = useState<UserData | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const refreshAuthenticatedUser = useCallback(
    async (fallbackUser: UserData | null) => {
      const requestToken = Storage.getToken();
      try {
        const userData = await userService.getMe();
        if (requestToken !== Storage.getToken()) {
          return;
        }

        const nextUser = normalizeAuthenticatedUser({
          id: fallbackUser?.id,
          firstName: userData.firstName ?? fallbackUser?.firstName ?? '',
          lastName: userData.lastName ?? fallbackUser?.lastName ?? '',
          username: userData.username ?? fallbackUser?.username ?? '',
          email: userData.email ?? fallbackUser?.email ?? '',
          role: userData.role ?? fallbackUser?.role,
          isVerified: userData.isVerified,
          isTwoFacAuth: userData.isTwoFacAuth,
          twoFactorEnabled: userData.isTwoFacAuth,
          provider: fallbackUser?.provider,
        });

        Storage.setUser(nextUser);
        setUser(nextUser);
      } catch {
        if (requestToken !== Storage.getToken()) {
          return;
        }

        if (fallbackUser) {
          Storage.setUser(fallbackUser);
          setUser(fallbackUser);
        }
      }
    },
    [],
  );

  // Initialize auth state from storage on mount
  useEffect(() => {
    const initAuth = () => {
      const token = Storage.getToken();
      const storedUser = Storage.getUser();
      const extractedUser = token ? extractUserFromToken(token) : null;

      if (token && storedUser) {
        const userToRestore = restoreAuthenticatedUser(
          storedUser.role || !extractedUser?.role
            ? storedUser
            : { ...storedUser, role: extractedUser.role },
          setUser,
        );
        void refreshAuthenticatedUser(userToRestore);
      } else if (token) {
        if (extractedUser) {
          const userToRestore = restoreAuthenticatedUser(extractedUser, setUser);
          void refreshAuthenticatedUser(userToRestore);
        } else {
          // Invalid token, clear storage
          Storage.clearAll();
        }
      }

      setIsLoading(false);
    };

    initAuth();
  }, [refreshAuthenticatedUser]);

  useEffect(() => {
    const handleNavigationRequest = (event: Event) => {
      const { path, clearAuth } = (event as CustomEvent<NavigationRequest>).detail;
      if (clearAuth) {
        Storage.clearAll();
        setUser(null);
        setIsLoading(false);
      }
      navigate(path);
    };

    window.addEventListener(NAVIGATION_REQUEST_EVENT, handleNavigationRequest);
    return () => window.removeEventListener(NAVIGATION_REQUEST_EVENT, handleNavigationRequest);
  }, [navigate]);

  const login = (token: string, userData?: UserData) => {
    Storage.setToken(token);

    // Use provided user data or extract from token
    const userToStore = userData || extractUserFromToken(token);

    if (userToStore) {
      const userToRestore = restoreAuthenticatedUser(userToStore, setUser);
      setIsLoading(false);
      void refreshAuthenticatedUser(userToRestore);
      return;
    }

    Storage.removeUser();
    setUser(null);
    setIsLoading(false);
    void refreshAuthenticatedUser(null);
  };

  const logout = () => {
    Storage.clearAll();
    setUser(null);
    setIsLoading(false);
  };

  const updateUser = (userData: UserData) => {
    Storage.setUser(userData);
    setUser(userData);
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    logout,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
