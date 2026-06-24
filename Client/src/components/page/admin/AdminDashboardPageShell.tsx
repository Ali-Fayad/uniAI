import type { UseAdminDashboardControllerReturn } from './useAdminDashboardController';
import AdminDashboardTabs from './AdminDashboardTabs';
import { AnimatedContent } from '../../animations';

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

        <AnimatedContent
          activeKey={controller.activeTab}
          className="w-full min-w-0 space-y-4"
          duration={0.2}
          yOffset={8}
        >
          {controller.activeTabDefinition.renderContent(controller)}
        </AnimatedContent>
      </div>
    </main>
  );
};

export default AdminDashboardPageShell;
