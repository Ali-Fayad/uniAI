type SelectedUserTab = 'statistics' | 'personal-info' | 'feedback';

interface SelectedUserTabsProps {
  activeTab: SelectedUserTab;
  onTabChange: (tab: SelectedUserTab) => void;
}

const tabs: Array<{ id: SelectedUserTab; label: string }> = [
  { id: 'statistics', label: 'Statistics' },
  { id: 'personal-info', label: 'Personal Info' },
  { id: 'feedback', label: 'Feedback' },
];

const tabButtonId = (tab: SelectedUserTab) => `selected-user-tab-${tab}`;
const tabPanelId = (tab: SelectedUserTab) => `selected-user-tabpanel-${tab}`;

const SelectedUserTabs = ({ activeTab, onTabChange }: SelectedUserTabsProps) => {
  return (
    <div
      role="tablist"
      aria-label="Selected user sections"
      className="flex flex-wrap gap-2 rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] p-2"
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

export { tabButtonId as selectedUserTabButtonId, tabPanelId as selectedUserTabPanelId };
export type { SelectedUserTab };
export default SelectedUserTabs;
