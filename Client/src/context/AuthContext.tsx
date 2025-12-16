import { createContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { Storage } from '../utils/Storage';
import { extractUserFromToken } from '../utils/JwtDecode';
import type { UserData } from '../types/dto';

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
  const [user, setUser] = useState<UserData | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialize auth state from storage on mount
  useEffect(() => {
    const initAuth = () => {
      const token = Storage.getToken();
      const storedUser = Storage.getUser();

      if (token && storedUser) {
        setUser(storedUser);
      } else if (token) {
        // Try to extract user from token if not in storage
        const extractedUser = extractUserFromToken(token);
        if (extractedUser) {
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

  const login = (token: string, userData?: UserData) => {
    Storage.setToken(token);

    // Use provided user data or extract from token
    const userToStore = userData || extractUserFromToken(token);
    
    if (userToStore) {
      Storage.setUser(userToStore);
      setUser(userToStore);
    }
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
    isAuthenticated: !!user,
    isLoading,
    login,
    logout,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
