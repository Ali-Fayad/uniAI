import { useCallback, useEffect, useState } from 'react';
import { adminService } from '../../../services/admin';
import type { AdminOverviewResponse } from '../../../types/dto';

export type AdminDashboardTab = 'statistics' | 'user-search';

export interface AdminDashboardTabMeta {
  id: AdminDashboardTab;
  label: string;
}

export interface UseAdminDashboardControllerReturn {
  activeTab: AdminDashboardTab;
  setActiveTab: (tab: AdminDashboardTab) => void;
  tabs: AdminDashboardTabMeta[];
  overview: AdminOverviewResponse | null;
  isOverviewLoading: boolean;
  overviewError: string | null;
  refreshOverview: () => Promise<void>;
}

export const useAdminDashboardController = (): UseAdminDashboardControllerReturn => {
  const [activeTab, setActiveTab] = useState<AdminDashboardTab>('statistics');
  const [overview, setOverview] = useState<AdminOverviewResponse | null>(null);
  const [isOverviewLoading, setIsOverviewLoading] = useState(false);
  const [overviewError, setOverviewError] = useState<string | null>(null);

  const loadOverview = useCallback(async () => {
    setIsOverviewLoading(true);
    setOverviewError(null);

    try {
      const data = await adminService.getOverview();
      setOverview(data);
    } catch {
      setOverview(null);
      setOverviewError('Unable to load overview statistics right now. Please try again.');
    } finally {
      setIsOverviewLoading(false);
    }
  }, []);

  const refreshOverview = useCallback(async () => {
    await loadOverview();
  }, [loadOverview]);

  useEffect(() => {
    void loadOverview();
  }, [loadOverview]);

  const tabs: AdminDashboardTabMeta[] = [
    { id: 'statistics', label: 'Statistics' },
    { id: 'user-search', label: 'User Search' },
  ];

  return {
    activeTab,
    setActiveTab,
    tabs,
    overview,
    isOverviewLoading,
    overviewError,
    refreshOverview,
  };
};
