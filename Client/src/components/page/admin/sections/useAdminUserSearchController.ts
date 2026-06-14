import { useCallback, useState } from 'react';
import { adminService } from '../../../../services/admin';
import type { AdminUserDetailsResponse, AdminUserSearchResponse } from '../../../../types/dto';

export interface UseAdminUserSearchControllerReturn {
  emailQuery: string;
  setEmailQuery: (value: string) => void;
  results: AdminUserSearchResponse[];
  selectedUser: AdminUserSearchResponse | null;
  isLoading: boolean;
  error: string | null;
  helperMessage: string | null;
  hasSearched: boolean;
  handleSearch: () => Promise<void>;
  handleSelectUser: (user: AdminUserSearchResponse) => void;
  handleCloseSelectedUser: () => void;
  handleUserUpdated: (updatedUser: AdminUserDetailsResponse) => void;
  handleUserDeleted: (userId: number) => void;
}

const MIN_SEARCH_LENGTH = 3;

export const useAdminUserSearchController = (): UseAdminUserSearchControllerReturn => {
  const [emailQuery, setEmailQuery] = useState('');
  const [results, setResults] = useState<AdminUserSearchResponse[]>([]);
  const [selectedUser, setSelectedUser] = useState<AdminUserSearchResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [helperMessage, setHelperMessage] = useState<string | null>(
    'Search by email to find a user.'
  );
  const [hasSearched, setHasSearched] = useState(false);

  const handleSearch = useCallback(async () => {
    const trimmedEmail = emailQuery.trim();

    setError(null);
    setSelectedUser(null);

    if (trimmedEmail.length < MIN_SEARCH_LENGTH) {
      setResults([]);
      setHasSearched(false);
      setHelperMessage('Enter at least 3 characters.');
      return;
    }

    setIsLoading(true);
    setHelperMessage(null);
    setHasSearched(true);

    try {
      const data = await adminService.searchUsers(trimmedEmail);
      setResults(data);
      setError(null);
      if (data.length === 0) {
        setSelectedUser(null);
      }
    } catch {
      setResults([]);
      setSelectedUser(null);
      setError('Unable to search users right now. Please try again.');
    } finally {
      setIsLoading(false);
    }
  }, [emailQuery]);

  const handleSelectUser = useCallback((user: AdminUserSearchResponse) => {
    setSelectedUser(user);
  }, []);

  const handleCloseSelectedUser = useCallback(() => {
    setSelectedUser(null);
  }, []);

  const handleUserUpdated = useCallback((updatedUser: AdminUserDetailsResponse) => {
    const nextUser: AdminUserSearchResponse = {
      id: updatedUser.id,
      email: updatedUser.email,
      username: updatedUser.username,
      firstName: updatedUser.firstName,
      lastName: updatedUser.lastName,
      role: updatedUser.role,
    };

    setResults((prev) => prev.map((user) => (user.id === updatedUser.id ? nextUser : user)));
    setSelectedUser((prev) => (prev?.id === updatedUser.id ? nextUser : prev));
  }, []);

  const handleUserDeleted = useCallback((userId: number) => {
    setResults((prev) => prev.filter((user) => user.id !== userId));
    setSelectedUser((prev) => (prev?.id === userId ? null : prev));
  }, []);

  return {
    emailQuery,
    setEmailQuery,
    results,
    selectedUser,
    isLoading,
    error,
    helperMessage,
    hasSearched,
    handleSearch,
    handleSelectUser,
    handleCloseSelectedUser,
    handleUserUpdated,
    handleUserDeleted,
  };
};
