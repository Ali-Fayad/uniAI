import { useCallback, useEffect, useRef, useState } from 'react';
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
  const currentUserIdRef = useRef<number | null>(null);
  const requestVersionRef = useRef(0);

  const resetDialogState = useCallback(() => {
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
  }, []);

  const loadPersonalInfo = useCallback(async (userId: number, requestVersion: number) => {
    setPersonalInfoLoading(true);
    setPersonalInfoError(null);

    try {
      const data = await adminService.getUserPersonalInfo(userId);
      if (requestVersionRef.current !== requestVersion || currentUserIdRef.current !== userId) {
        return;
      }
      setPersonalInfo(data);
    } catch {
      if (requestVersionRef.current !== requestVersion || currentUserIdRef.current !== userId) {
        return;
      }
      setPersonalInfo(null);
      setPersonalInfoError('Unable to load personal information right now. Please try again.');
    } finally {
      if (requestVersionRef.current !== requestVersion || currentUserIdRef.current !== userId) {
        return;
      }
      setPersonalInfoLoading(false);
      setHasLoadedPersonalInfo(true);
    }
  }, []);

  const loadFeedback = useCallback(async (userId: number, requestVersion: number) => {
    setFeedbackLoading(true);
    setFeedbackError(null);

    try {
      const data = await adminService.getUserFeedback(userId);
      if (requestVersionRef.current !== requestVersion || currentUserIdRef.current !== userId) {
        return;
      }
      setFeedback(data);
    } catch {
      if (requestVersionRef.current !== requestVersion || currentUserIdRef.current !== userId) {
        return;
      }
      setFeedback([]);
      setFeedbackError('Unable to load feedback right now. Please try again.');
    } finally {
      if (requestVersionRef.current !== requestVersion || currentUserIdRef.current !== userId) {
        return;
      }
      setFeedbackLoading(false);
      setHasLoadedFeedback(true);
    }
  }, []);

  useEffect(() => {
    requestVersionRef.current += 1;
    const requestVersion = requestVersionRef.current;

    currentUserIdRef.current = selectedUserId;
    resetDialogState();

    if (selectedUserId === null) {
      return;
    }

    let isActive = true;

    const loadDetails = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const data = await adminService.getUserDetails(selectedUserId);
        if (!isActive || requestVersionRef.current !== requestVersion || currentUserIdRef.current !== selectedUserId) {
          return;
        }
        setUserDetails(data);
      } catch {
        if (!isActive || requestVersionRef.current !== requestVersion || currentUserIdRef.current !== selectedUserId) {
          return;
        }
        setUserDetails(null);
        setError('Unable to load user details right now. Please try again.');
      } finally {
        if (!isActive || requestVersionRef.current !== requestVersion || currentUserIdRef.current !== selectedUserId) {
          return;
        }
        setIsLoading(false);
      }
    };

    void loadDetails();

    return () => {
      isActive = false;
    };
  }, [resetDialogState, selectedUserId]);

  useEffect(() => {
    if (selectedUserId === null) {
      return;
    }

    const requestVersion = requestVersionRef.current;

    if (activeTab === 'personal-info' && !hasLoadedPersonalInfo && !personalInfoLoading) {
      void loadPersonalInfo(selectedUserId, requestVersion);
    }

    if (activeTab === 'feedback' && !hasLoadedFeedback && !feedbackLoading) {
      void loadFeedback(selectedUserId, requestVersion);
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
      if (currentUserIdRef.current !== updatedUser.id) {
        return false;
      }
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
