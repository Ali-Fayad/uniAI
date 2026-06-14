import { useCallback, useEffect, useState } from 'react';
import { isAxiosError } from 'axios';
import { adminService } from '../../../../services/admin';
import { useNotification } from '../../../../hooks/useNotification';
import type {
  AdminUserDetailsResponse,
  AdminUserFeedbackResponse,
  AdminUserPersonalInfoResponse,
  AdminUserSearchResponse,
  UserRole,
} from '../../../../types/dto';
import type { SelectedUserTab } from './SelectedUserTabs';

type AdminActionKind = 'role' | 'delete';

const getFriendlyAdminActionError = (error: unknown, action: AdminActionKind) => {
  if (isAxiosError(error)) {
    const status = error.response?.status;
    if (status === 403) {
      return action === 'role'
        ? 'You cannot demote yourself.'
        : 'You cannot delete your own account.';
    }
    if (status === 409) {
      return 'At least one admin must remain.';
    }
    if (status === 404) {
      return 'This user no longer exists.';
    }
  }

  return 'Something went wrong. Please try again.';
};

export interface UseSelectedUserDialogControllerReturn {
  userDetails: AdminUserDetailsResponse | null;
  isLoading: boolean;
  error: string | null;
  activeTab: SelectedUserTab;
  setActiveTab: (tab: SelectedUserTab) => void;
  personalInfo: AdminUserPersonalInfoResponse | null;
  personalInfoLoading: boolean;
  personalInfoError: string | null;
  feedback: AdminUserFeedbackResponse[];
  feedbackLoading: boolean;
  feedbackError: string | null;
  isRoleUpdating: boolean;
  roleActionError: string | null;
  isDeleting: boolean;
  deleteError: string | null;
  clearRoleActionError: () => void;
  clearDeleteError: () => void;
  toggleUserRole: () => Promise<boolean>;
  deleteUser: () => Promise<boolean>;
}

interface UseSelectedUserDialogControllerArgs {
  selectedUser: AdminUserSearchResponse | null;
  onUserUpdated: (updatedUser: AdminUserDetailsResponse) => void;
  onUserDeleted: (userId: number) => void;
}

export const useSelectedUserDialogController = (
  { selectedUser, onUserUpdated, onUserDeleted }: UseSelectedUserDialogControllerArgs,
): UseSelectedUserDialogControllerReturn => {
  const { showNotification } = useNotification();

  const [userDetails, setUserDetails] = useState<AdminUserDetailsResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<SelectedUserTab>('statistics');

  const [personalInfo, setPersonalInfo] = useState<AdminUserPersonalInfoResponse | null>(null);
  const [personalInfoLoading, setPersonalInfoLoading] = useState(false);
  const [personalInfoError, setPersonalInfoError] = useState<string | null>(null);
  const [hasLoadedPersonalInfo, setHasLoadedPersonalInfo] = useState(false);

  const [feedback, setFeedback] = useState<AdminUserFeedbackResponse[]>([]);
  const [feedbackLoading, setFeedbackLoading] = useState(false);
  const [feedbackError, setFeedbackError] = useState<string | null>(null);
  const [hasLoadedFeedback, setHasLoadedFeedback] = useState(false);

  const [isRoleUpdating, setIsRoleUpdating] = useState(false);
  const [roleActionError, setRoleActionError] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const selectedUserId = selectedUser?.id ?? null;

  const loadPersonalInfo = useCallback(async () => {
    if (selectedUserId === null) {
      return;
    }

    setPersonalInfoLoading(true);
    setPersonalInfoError(null);

    try {
      const data = await adminService.getUserPersonalInfo(selectedUserId);
      setPersonalInfo(data);
    } catch {
      setPersonalInfo(null);
      setPersonalInfoError('Unable to load personal information right now. Please try again.');
    } finally {
      setPersonalInfoLoading(false);
      setHasLoadedPersonalInfo(true);
    }
  }, [selectedUserId]);

  const loadFeedback = useCallback(async () => {
    if (selectedUserId === null) {
      return;
    }

    setFeedbackLoading(true);
    setFeedbackError(null);

    try {
      const data = await adminService.getUserFeedback(selectedUserId);
      setFeedback(data);
    } catch {
      setFeedback([]);
      setFeedbackError('Unable to load feedback right now. Please try again.');
    } finally {
      setFeedbackLoading(false);
      setHasLoadedFeedback(true);
    }
  }, [selectedUserId]);

  useEffect(() => {
    if (selectedUserId === null) {
      setUserDetails(null);
      setIsLoading(false);
      setError(null);
      setActiveTab('statistics');
      setPersonalInfo(null);
      setPersonalInfoLoading(false);
      setPersonalInfoError(null);
      setHasLoadedPersonalInfo(false);
      setFeedback([]);
      setFeedbackLoading(false);
      setFeedbackError(null);
      setHasLoadedFeedback(false);
      setIsRoleUpdating(false);
      setRoleActionError(null);
      setIsDeleting(false);
      setDeleteError(null);
      return;
    }

    let isActive = true;

    const loadDetails = async () => {
      setIsLoading(true);
      setError(null);
      setActiveTab('statistics');

      try {
        const data = await adminService.getUserDetails(selectedUserId);
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
  }, [selectedUserId]);

  useEffect(() => {
    if (selectedUserId === null) {
      return;
    }

    if (activeTab === 'personal-info' && !hasLoadedPersonalInfo && !personalInfoLoading) {
      void loadPersonalInfo();
    }

    if (activeTab === 'feedback' && !hasLoadedFeedback && !feedbackLoading) {
      void loadFeedback();
    }
  }, [
    activeTab,
    feedbackLoading,
    hasLoadedFeedback,
    hasLoadedPersonalInfo,
    loadFeedback,
    loadPersonalInfo,
    personalInfoLoading,
    selectedUserId,
  ]);

  const toggleUserRole = useCallback(async () => {
    if (!userDetails) {
      return false;
    }

    const targetRole: UserRole = userDetails.role === 'ADMIN' ? 'USER' : 'ADMIN';
    setIsRoleUpdating(true);
    setRoleActionError(null);

    try {
      const updatedUser = await adminService.updateUserRole(userDetails.id, targetRole);
      setUserDetails(updatedUser);
      onUserUpdated(updatedUser);
      showNotification({
        type: 'success',
        message: `${updatedUser.username} updated to ${updatedUser.role === 'ADMIN' ? 'Admin' : 'User'}.`,
      });
      return true;
    } catch (error) {
      setRoleActionError(getFriendlyAdminActionError(error, 'role'));
      return false;
    } finally {
      setIsRoleUpdating(false);
    }
  }, [onUserUpdated, showNotification, userDetails]);

  const deleteUser = useCallback(async () => {
    if (!userDetails) {
      return false;
    }

    setIsDeleting(true);
    setDeleteError(null);

    try {
      await adminService.deleteUser(userDetails.id);
      onUserDeleted(userDetails.id);
      showNotification({
        type: 'success',
        message: `${userDetails.username} deleted successfully.`,
      });
      return true;
    } catch (error) {
      setDeleteError(getFriendlyAdminActionError(error, 'delete'));
      return false;
    } finally {
      setIsDeleting(false);
    }
  }, [onUserDeleted, showNotification, userDetails]);

  const clearRoleActionError = useCallback(() => {
    setRoleActionError(null);
  }, []);

  const clearDeleteError = useCallback(() => {
    setDeleteError(null);
  }, []);

  return {
    userDetails,
    isLoading,
    error,
    activeTab,
    setActiveTab,
    personalInfo,
    personalInfoLoading,
    personalInfoError,
    feedback,
    feedbackLoading,
    feedbackError,
    isRoleUpdating,
    roleActionError,
    isDeleting,
    deleteError,
    clearRoleActionError,
    clearDeleteError,
    toggleUserRole,
    deleteUser,
  };
};
