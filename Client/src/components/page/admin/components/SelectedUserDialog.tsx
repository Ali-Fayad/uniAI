import { useMemo, useRef } from 'react';
import { X } from 'lucide-react';
import LoadingSpinner from '../../../common/LoadingSpinner';
import { useOnClickOutside } from '../../../../hooks/useOnClickOutside';
import type { AdminUserSearchResponse } from '../../../../types/dto';
import UserRoleBadge from './UserRoleBadge';
import SelectedUserTabs, { selectedUserTabButtonId, selectedUserTabPanelId, type SelectedUserTab } from './SelectedUserTabs';
import SelectedUserStatisticsTab from './SelectedUserStatisticsTab';
import SelectedUserPersonalInfoTab from './SelectedUserPersonalInfoTab';
import SelectedUserFeedbackTab from './SelectedUserFeedbackTab';
import { useSelectedUserDialogController } from './useSelectedUserDialogController';

interface SelectedUserDialogProps {
  user: AdminUserSearchResponse | null;
  onClose: () => void;
}

const tabPlaceholderId = (tab: SelectedUserTab) => selectedUserTabPanelId(tab);

const SelectedUserDialog = ({ user, onClose }: SelectedUserDialogProps) => {
  const panelRef = useRef<HTMLDivElement>(null);
  const controller = useSelectedUserDialogController(user);

  useOnClickOutside(panelRef, () => onClose(), { enabled: !!user });

  const headerBadges = useMemo(() => {
    if (!controller.userDetails) {
      return null;
    }

    return (
      <div className="flex flex-wrap gap-2">
        <span className="rounded-full border border-[var(--color-border)] bg-[var(--color-elevatedSurface)] px-2.5 py-1 text-xs font-semibold text-[var(--color-textSecondary)]">
          {controller.userDetails.isVerified ? 'Verified' : 'Unverified'}
        </span>
        <span className="rounded-full border border-[var(--color-border)] bg-[var(--color-elevatedSurface)] px-2.5 py-1 text-xs font-semibold text-[var(--color-textSecondary)]">
          {controller.userDetails.isTwoFacAuth ? '2FA Enabled' : '2FA Disabled'}
        </span>
      </div>
    );
  }, [controller.userDetails]);

  if (!user) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <div
        ref={panelRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="selected-user-dialog-title"
        className="relative w-full max-w-5xl max-h-[90vh] overflow-y-auto rounded-3xl border border-[var(--color-border)] bg-[var(--color-surface)] shadow-2xl"
      >
        <div className="sticky top-0 z-20 border-b border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-4 sm:px-6">
          <div className="flex items-start justify-between gap-4">
            <div className="space-y-2">
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-[var(--color-primary)]">
                Selected User
              </p>
              <div className="flex flex-wrap items-center gap-2">
                <h2 id="selected-user-dialog-title" className="text-2xl font-black tracking-tight text-[var(--color-textPrimary)]">
                  {controller.userDetails?.username ?? user.username}
                </h2>
                {controller.userDetails ? (
                  <UserRoleBadge role={controller.userDetails.role} />
                ) : (
                  <UserRoleBadge role={user.role} />
                )}
              </div>
              <p className="text-sm text-[var(--color-textSecondary)]">
                {controller.userDetails?.email ?? user.email}
              </p>
              {headerBadges}
            </div>

            <button
              type="button"
              onClick={onClose}
              className="rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] p-2 text-[var(--color-textSecondary)] transition-colors hover:text-[var(--color-textPrimary)]"
              aria-label="Close selected user dialog"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>

        <div className="space-y-6 px-5 py-5 sm:px-6 sm:py-6">
          {controller.isLoading && (
            <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-10">
              <LoadingSpinner text="Loading user details..." />
            </div>
          )}

          {controller.error && !controller.isLoading && (
            <div
              role="alert"
              className="rounded-2xl border border-[var(--color-error)]/40 bg-[var(--color-error)]/10 px-4 py-4 text-sm text-[var(--color-textPrimary)]"
            >
              {controller.error}
            </div>
          )}

          {!controller.isLoading && !controller.error && controller.userDetails && (
            <div className="space-y-4">
              <SelectedUserTabs
                activeTab={controller.activeTab}
                onTabChange={controller.setActiveTab}
              />

              {controller.activeTab === 'statistics' && (
                <section
                  id={tabPlaceholderId('statistics')}
                  role="tabpanel"
                  aria-labelledby={selectedUserTabButtonId('statistics')}
                  className="space-y-4"
                >
                  <SelectedUserStatisticsTab userDetails={controller.userDetails} />
                </section>
              )}

              {controller.activeTab === 'personal-info' && (
                <section
                  id={tabPlaceholderId('personal-info')}
                  role="tabpanel"
                  aria-labelledby={selectedUserTabButtonId('personal-info')}
                  className="space-y-4"
                >
                  <SelectedUserPersonalInfoTab
                    personalInfo={controller.personalInfo}
                    isLoading={controller.personalInfoLoading}
                    error={controller.personalInfoError}
                  />
                </section>
              )}

              {controller.activeTab === 'feedback' && (
                <section
                  id={tabPlaceholderId('feedback')}
                  role="tabpanel"
                  aria-labelledby={selectedUserTabButtonId('feedback')}
                  className="space-y-4"
                >
                  <SelectedUserFeedbackTab
                    feedback={controller.feedback}
                    isLoading={controller.feedbackLoading}
                    error={controller.feedbackError}
                  />
                </section>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SelectedUserDialog;
