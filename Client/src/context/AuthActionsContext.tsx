import { createContext } from 'react';
import type { UserData } from '../types/user';

/**
 * ISP: write-only auth actions — consumed by components that trigger
 * login/logout/profile updates (e.g. SignIn, SignUp, Header).
 */
export interface AuthActions {
  login: (token: string, userData?: UserData) => void;
  logout: () => void;
  updateUser: (userData: UserData) => void;
}

export const AuthActionsContext = createContext<AuthActions | undefined>(undefined);
