import type { UserData } from '../types/dto';

/**
 * IStorageService
 *
 * Abstraction for persisting authentication tokens and user data.
 * Consumers depend on this interface, not on the concrete StorageService,
 * satisfying the Dependency Inversion Principle.
 */
export interface IStorageService {
  getToken(): string | null;
  setToken(token: string): void;
  removeToken(): void;

  getUser(): UserData | null;
  setUser(user: UserData): void;
  removeUser(): void;

  isAuthenticated(): boolean;
  clearAll(): void;
}
