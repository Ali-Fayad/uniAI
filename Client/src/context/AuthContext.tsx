import { createContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { Storage } from '../utils/Storage';
import { extractUserFromToken } from '../utils/JwtDecode';
import type { UserData } from '../types/dto';
import { NAVIGATION_REQUEST_EVENT } from '../events/navigationEvents';
import type { NavigationRequest } from '../events/navigationEvents';

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

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const navigate = useNavigate();
  const [user, setUser] = useState<UserData | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialize auth state from storage on mount
  useEffect(() => {
    const initAuth = () => {
      const token = Storage.getToken();
      const storedUser = Storage.getUser();

      if (token && storedUser && storedUser.isVerified !== false) {
        setUser(storedUser);
      } else if (token) {
        // Try to extract user from token if not in storage
        const extractedUser = extractUserFromToken(token);
        if (extractedUser && extractedUser.isVerified !== false) {
          setUser(extractedUser);
          Storage.setUser(extractedUser);
        } else {
          // Invalid token, clear storage
          Storage.clearAll();
        }
      }

      setIsLoading(false);
    };

    initAuth();
  }, []);

  useEffect(() => {
    const handleNavigationRequest = (event: Event) => {
      const { path, clearAuth } = (event as CustomEvent<NavigationRequest>).detail;
      if (clearAuth) {
        Storage.clearAll();
        setUser(null);
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
    
    if (userToStore && userToStore.isVerified !== false) {
      Storage.setUser(userToStore);
      setUser(userToStore);
      return;
    }

    Storage.removeUser();
    setUser(null);
  };

  const logout = () => {
    Storage.clearAll();
    setUser(null);
  };

  const updateUser = (userData: UserData) => {
    Storage.setUser(userData);
    setUser(userData);
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user && user.isVerified !== false,
    isLoading,
    login,
    logout,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
