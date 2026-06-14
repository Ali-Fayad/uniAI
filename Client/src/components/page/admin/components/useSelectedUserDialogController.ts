import { useEffect, useState } from 'react';
import { adminService } from '../../../../services/admin';
import type { AdminUserDetailsResponse, AdminUserSearchResponse } from '../../../../types/dto';
import type { SelectedUserTab } from './SelectedUserTabs';

export interface UseSelectedUserDialogControllerReturn {
  userDetails: AdminUserDetailsResponse | null;
  isLoading: boolean;
  error: string | null;
  activeTab: SelectedUserTab;
  setActiveTab: (tab: SelectedUserTab) => void;
}

export const useSelectedUserDialogController = (
  selectedUser: AdminUserSearchResponse | null,
): UseSelectedUserDialogControllerReturn => {
  const [userDetails, setUserDetails] = useState<AdminUserDetailsResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<SelectedUserTab>('statistics');

  useEffect(() => {
    if (!selectedUser) {
      setUserDetails(null);
      setIsLoading(false);
      setError(null);
      setActiveTab('statistics');
      return;
    }

    let isActive = true;

    const loadDetails = async () => {
      setIsLoading(true);
      setError(null);
      setActiveTab('statistics');

      try {
        const data = await adminService.getUserDetails(selectedUser.id);
        if (!isActive) {
          return;
        }
        setUserDetails(data);
      } catch {
        if (!isActive) {
          return;
        }
        setUserDetails(null);
        setError('Unable to load user details right now. Please try again.');
      } finally {
        if (isActive) {
          setIsLoading(false);
        }
      }
    };

    void loadDetails();

    return () => {
      isActive = false;
    };
  }, [selectedUser]);

  return {
    userDetails,
    isLoading,
    error,
    activeTab,
    setActiveTab,
  };
};
