import type { ReactNode } from 'react';
import { BarChart3, Search } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { adminService } from '../../../services/admin';
import type { AdminOverviewResponse } from '../../../types/dto';
import { adminDashboardTabButtonId, adminDashboardTabPanelId } from './AdminDashboardTabs';
import AdminStatisticsSection from './sections/AdminStatisticsSection';
import AdminUserSearchSection from './sections/AdminUserSearchSection';
import AdminCatalogueSection from './sections/AdminCatalogueSection';
import AdminPromptsSection from './sections/AdminPromptsSection';

export interface AdminDashboardContentContext {
  overview: AdminOverviewResponse | null;
  isOverviewLoading: boolean;
  overviewError: string | null;
  refreshOverview: () => Promise<void>;
}

export interface AdminDashboardTabDefinition {
  id: string;
  label: string;
  description?: string;
  icon?: ReactNode;
  renderContent: (context: AdminDashboardContentContext) => ReactNode;
}

const adminDashboardTabs = [
  {
    id: 'statistics',
    label: 'Statistics',
    description: 'Platform usage at a glance',
    icon: <BarChart3 className="h-4 w-4" aria-hidden="true" />,
    renderContent: ({ overview, isOverviewLoading, overviewError, refreshOverview }) => (
      <AdminStatisticsSection
        id={adminDashboardTabPanelId('statistics')}
        labelledBy={adminDashboardTabButtonId('statistics')}
        overview={overview}
        isLoading={isOverviewLoading}
        error={overviewError}
        onRetry={refreshOverview}
      />
    ),
  },
  {
    id: 'user-search',
    label: 'User Search',
    description: 'Look up and inspect accounts',
    icon: <Search className="h-4 w-4" aria-hidden="true" />,
    renderContent: () => (
      <AdminUserSearchSection
        id={adminDashboardTabPanelId('user-search')}
        labelledBy={adminDashboardTabButtonId('user-search')}
      />
    ),
  },
  {
    id: 'catalogue', label: 'Catalogue', description: 'Manage skills and positions',
    renderContent: () => <AdminCatalogueSection />,
  },
  {
    id: 'ai-prompts', label: 'AI Prompts', description: 'Inspect active prompt resources',
    renderContent: () => <AdminPromptsSection />,
  },
] as const satisfies readonly AdminDashboardTabDefinition[];

export type AdminDashboardTab = (typeof adminDashboardTabs)[number]['id'];
export type AdminDashboardTabMeta = (typeof adminDashboardTabs)[number];

export interface UseAdminDashboardControllerReturn {
  activeTab: AdminDashboardTab;
  setActiveTab: (tab: AdminDashboardTab) => void;
  tabs: typeof adminDashboardTabs;
  activeTabDefinition: AdminDashboardTabMeta;
  overview: AdminOverviewResponse | null;
  isOverviewLoading: boolean;
  overviewError: string | null;
  refreshOverview: () => Promise<void>;
}

const getActiveTabDefinition = (tabs: typeof adminDashboardTabs, activeTab: AdminDashboardTab) => {
  return tabs.find((tab) => tab.id === activeTab) ?? tabs[0];
};

export const useAdminDashboardController = (): UseAdminDashboardControllerReturn => {
  const [activeTab, setActiveTab] = useState<AdminDashboardTab>(adminDashboardTabs[0].id);
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

  const activeTabDefinition = getActiveTabDefinition(adminDashboardTabs, activeTab);

  return {
    activeTab,
    setActiveTab,
    tabs: adminDashboardTabs,
    activeTabDefinition,
    overview,
    isOverviewLoading,
    overviewError,
    refreshOverview,
  };
};
