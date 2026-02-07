import { STORAGE_KEYS } from '../constants';
import type { UserData } from '../types/dto';

/**
 * Storage utility for managing authentication tokens and user data in sessionStorage
 */
class StorageService {
  // Token management
  getToken(): string | null {
    return sessionStorage.getItem(STORAGE_KEYS.TOKEN);
  }

  setToken(token: string): void {
    sessionStorage.setItem(STORAGE_KEYS.TOKEN, token);
  }

  removeToken(): void {
    sessionStorage.removeItem(STORAGE_KEYS.TOKEN);
  }

  // User data management
  getUser(): UserData | null {
    const userData = sessionStorage.getItem(STORAGE_KEYS.USER);
    if (!userData) return null;
    
    try {
      return JSON.parse(userData) as UserData;
    } catch (error) {
      console.error('Failed to parse user data:', error);
      return null;
    }
  }

  setUser(user: UserData): void {
    sessionStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
  }

  removeUser(): void {
    sessionStorage.removeItem(STORAGE_KEYS.USER);
  }

  // Authentication check
  isAuthenticated(): boolean {
    const token = this.getToken();
    return token !== null && token.length > 0;
  }

  // Clear all stored data
  clearAll(): void {
    this.removeToken();
    this.removeUser();
  }
}

// Export a singleton instance
export const Storage = new StorageService();
