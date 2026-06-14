import { useState } from 'react';

export type AdminDashboardTab = 'statistics' | 'user-search';

export interface AdminDashboardTabMeta {
  id: AdminDashboardTab;
  label: string;
}

export interface UseAdminDashboardControllerReturn {
  activeTab: AdminDashboardTab;
  setActiveTab: (tab: AdminDashboardTab) => void;
  tabs: AdminDashboardTabMeta[];
}

export const useAdminDashboardController = (): UseAdminDashboardControllerReturn => {
  const [activeTab, setActiveTab] = useState<AdminDashboardTab>('statistics');

  const tabs: AdminDashboardTabMeta[] = [
    { id: 'statistics', label: 'Statistics' },
    { id: 'user-search', label: 'User Search' },
  ];

  return {
    activeTab,
    setActiveTab,
    tabs,
  };
};
