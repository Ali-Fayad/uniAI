import type { AdminDashboardTab, AdminDashboardTabMeta } from './useAdminDashboardController';

interface AdminDashboardTabsProps {
  tabs: AdminDashboardTabMeta[];
  activeTab: AdminDashboardTab;
  onTabChange: (tab: AdminDashboardTab) => void;
}

const tabPanelId = (tab: AdminDashboardTab) => `admin-dashboard-tabpanel-${tab}`;
const tabButtonId = (tab: AdminDashboardTab) => `admin-dashboard-tab-${tab}`;

const AdminDashboardTabs = ({ tabs, activeTab, onTabChange }: AdminDashboardTabsProps) => {
  return (
    <div
      role="tablist"
      aria-label="Admin dashboard sections"
      className="flex flex-wrap gap-2 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-2"
    >
      {tabs.map((tab) => {
        const selected = activeTab === tab.id;

        return (
          <button
            key={tab.id}
            id={tabButtonId(tab.id)}
            type="button"
            role="tab"
            aria-selected={selected}
            aria-controls={tabPanelId(tab.id)}
            onClick={() => onTabChange(tab.id)}
            className={[
              'inline-flex items-center justify-center rounded-xl px-4 py-2 text-sm font-semibold transition-colors',
              selected
                ? 'bg-[var(--color-primary)] text-[var(--color-background)] shadow-sm'
                : 'text-[var(--color-textSecondary)] hover:bg-[var(--color-elevatedSurface)] hover:text-[var(--color-textPrimary)]',
            ].join(' ')}
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
};

export const adminDashboardTabPanelId = tabPanelId;
export const adminDashboardTabButtonId = tabButtonId;

export default AdminDashboardTabs;
