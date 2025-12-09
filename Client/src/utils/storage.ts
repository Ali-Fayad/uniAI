import type { AuthenticationResponseDto } from "./auth";

const TOKEN_KEY = "token";
const DATA_KEY = "data";
const EMAIL_KEY = "emailForVerification";

export const TokenStorage = {
  saveToken: (token: string) => localStorage.setItem(TOKEN_KEY, token),

  getToken: () => localStorage.getItem(TOKEN_KEY),

  saveData: (data: AuthenticationResponseDto) =>
    localStorage.setItem(DATA_KEY, JSON.stringify(data)),

  getData: (): AuthenticationResponseDto | null => {
    const data = localStorage.getItem(DATA_KEY);
    return data ? JSON.parse(data) : null;
  },

  saveEmailForVerification: (email: string) =>
    localStorage.setItem(EMAIL_KEY, email),

  getEmailForVerification: () => localStorage.getItem(EMAIL_KEY),

  clearAll: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(DATA_KEY);
    localStorage.removeItem(EMAIL_KEY);
  },

  isAuthenticated: (): boolean => {
    const token = localStorage.getItem(TOKEN_KEY);
    const data = localStorage.getItem(DATA_KEY);
    return !!token && !!data;
  },

  logout: (): boolean => {
    try {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(DATA_KEY);
      localStorage.removeItem(EMAIL_KEY);
      return true;
    } catch (_) {
      return false;
    }
  }
};
