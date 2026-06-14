import type { UseAdminDashboardControllerReturn } from './useAdminDashboardController';
import AdminDashboardTabs, { adminDashboardTabButtonId, adminDashboardTabPanelId } from './AdminDashboardTabs';
import AdminStatisticsSection from './sections/AdminStatisticsSection';
import AdminUserSearchSection from './sections/AdminUserSearchSection';

interface AdminDashboardPageShellProps {
  controller: UseAdminDashboardControllerReturn;
}

const AdminDashboardPageShell = ({ controller }: AdminDashboardPageShellProps) => {
  return (
    <main className="min-h-[calc(100vh-64px)] bg-[var(--color-background)] px-4 py-8 sm:px-6 lg:px-8">
      <div className="mx-auto w-full max-w-6xl space-y-6">
        <header className="space-y-2">
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-[var(--color-primary)]">
            Admin Dashboard
          </p>
          <h1 className="text-3xl font-black tracking-tight text-[var(--color-textPrimary)] sm:text-4xl">
            Admin Dashboard
          </h1>
          <p className="max-w-3xl text-sm leading-6 text-[var(--color-textSecondary)] sm:text-base">
            Use the dashboard tabs to inspect platform statistics or search users by email.
          </p>
        </header>

        <AdminDashboardTabs
          tabs={controller.tabs}
          activeTab={controller.activeTab}
          onTabChange={controller.setActiveTab}
        />

        <div className="space-y-4">
          {controller.activeTab === 'statistics' && (
            <AdminStatisticsSection
              id={adminDashboardTabPanelId('statistics')}
              labelledBy={adminDashboardTabButtonId('statistics')}
              overview={controller.overview}
              isLoading={controller.isOverviewLoading}
              error={controller.overviewError}
              onRetry={controller.refreshOverview}
            />
          )}

          {controller.activeTab === 'user-search' && (
            <AdminUserSearchSection
              id={adminDashboardTabPanelId('user-search')}
              labelledBy={adminDashboardTabButtonId('user-search')}
            />
          )}
        </div>
      </div>
    </main>
  );
};

export default AdminDashboardPageShell;
