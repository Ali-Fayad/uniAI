import type { AdminDashboardTab, AdminDashboardTabMeta } from './useAdminDashboardController';

interface AdminDashboardTabsProps {
  tabs: readonly AdminDashboardTabMeta[];
  activeTab: AdminDashboardTab;
  onTabChange: (tab: AdminDashboardTab) => void;
}

const tabPanelId = (tab: AdminDashboardTab) => `admin-dashboard-tabpanel-${tab}`;
const tabButtonId = (tab: AdminDashboardTab) => `admin-dashboard-tab-${tab}`;

const AdminDashboardTabs = ({ tabs, activeTab, onTabChange }: AdminDashboardTabsProps) => {
  return (
    <div className="w-full overflow-x-auto">
      <div
        role="tablist"
        aria-label="Admin dashboard sections"
        className="grid min-w-[20rem] gap-2 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-2 sm:gap-3 sm:min-w-0"
        style={{ gridTemplateColumns: `repeat(${tabs.length}, minmax(0, 1fr))` }}
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
              tabIndex={selected ? 0 : -1}
              onClick={() => onTabChange(tab.id)}
              className={[
                'flex min-h-16 w-full flex-col items-center justify-center gap-1 rounded-xl px-4 py-3 text-center transition-colors',
                selected
                  ? 'bg-[var(--color-primary)] text-[var(--color-background)] shadow-sm'
                  : 'text-[var(--color-textSecondary)] hover:bg-[var(--color-elevatedSurface)] hover:text-[var(--color-textPrimary)]',
              ].join(' ')}
            >
              {tab.icon ? <span aria-hidden="true" className="flex items-center justify-center">{tab.icon}</span> : null}
              <span className="text-sm font-semibold leading-tight">{tab.label}</span>
              {tab.description ? (
                <span
                  className={[
                    'text-xs leading-4',
                    selected ? 'text-[var(--color-background)]/85' : 'text-[var(--color-textSecondary)]',
                  ].join(' ')}
                >
                  {tab.description}
                </span>
              ) : null}
            </button>
          );
        })}
      </div>
    </div>
  );
};

export const adminDashboardTabPanelId = tabPanelId;
export const adminDashboardTabButtonId = tabButtonId;

export default AdminDashboardTabs;
