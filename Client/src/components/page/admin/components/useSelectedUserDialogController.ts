import { useCallback, useEffect, useState } from 'react';
import { adminService } from '../../../../services/admin';
import type {
  AdminUserDetailsResponse,
  AdminUserFeedbackResponse,
  AdminUserPersonalInfoResponse,
  AdminUserSearchResponse,
} from '../../../../types/dto';
import type { SelectedUserTab } from './SelectedUserTabs';

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
}

export const useSelectedUserDialogController = (
  selectedUser: AdminUserSearchResponse | null,
): UseSelectedUserDialogControllerReturn => {
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
    if (!selectedUser) {
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
  };
};
