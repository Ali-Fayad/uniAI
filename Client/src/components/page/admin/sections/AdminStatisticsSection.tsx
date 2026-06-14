import LoadingSpinner from '../../../common/LoadingSpinner';
import type { AdminOverviewResponse } from '../../../../types/dto';
import UserStatCard from '../components/UserStatCard';

interface AdminStatisticsSectionProps {
  id: string;
  labelledBy: string;
  overview: AdminOverviewResponse | null;
  isLoading: boolean;
  error: string | null;
  onRetry: () => Promise<void>;
}

const zeroOverview: AdminOverviewResponse = {
  totalUsers: 0,
  totalChats: 0,
  totalMessages: 0,
  totalFeedback: 0,
  averageChatsPerUser: 0,
  averageMessagesPerChat: 0,
  averageMessagesPerUser: 0,
};

const formatCount = (value: number) => value.toLocaleString();
const formatRatio = (value: number) => value.toFixed(2);

const AdminStatisticsSection = ({
  id,
  labelledBy,
  overview,
  isLoading,
  error,
  onRetry,
}: AdminStatisticsSectionProps) => {
  const data = overview ?? zeroOverview;

  return (
    <section
      id={id}
      role="tabpanel"
      aria-labelledby={labelledBy}
      className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-8 sm:px-6"
    >
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div className="space-y-2">
          <p className="text-base font-semibold text-[var(--color-textPrimary)]">Statistics</p>
          <p className="text-sm leading-6 text-[var(--color-textSecondary)]">
            Snapshot of current platform usage.
          </p>
        </div>

        <button
          type="button"
          onClick={() => void onRetry()}
          disabled={isLoading}
          className="inline-flex items-center justify-center rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-2.5 text-sm font-semibold text-[var(--color-textPrimary)] transition-colors hover:bg-[var(--color-elevatedSurface)] disabled:cursor-not-allowed disabled:opacity-70"
        >
          Refresh
        </button>
      </div>

      {isLoading && (
        <div className="mt-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-10">
          <LoadingSpinner text="Loading overview statistics..." />
        </div>
      )}

      {error && !isLoading && (
        <div className="mt-6 space-y-4">
          <div
            role="alert"
            className="rounded-2xl border border-[var(--color-error)]/40 bg-[var(--color-error)]/10 px-4 py-4 text-sm text-[var(--color-textPrimary)]"
          >
            {error}
          </div>
          <button
            type="button"
            onClick={() => void onRetry()}
            className="inline-flex items-center justify-center rounded-xl bg-[var(--color-primary)] px-4 py-2.5 text-sm font-semibold text-[var(--color-background)] transition-colors hover:opacity-95"
          >
            Retry
          </button>
        </div>
      )}

      {!isLoading && !error && (
        <div className="mt-6 space-y-6">
          <div className="space-y-3">
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-[var(--color-textSecondary)]">
              Totals
            </p>
            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
              <UserStatCard label="Total Users" value={formatCount(data.totalUsers)} />
              <UserStatCard label="Total Chats" value={formatCount(data.totalChats)} />
              <UserStatCard label="Total Messages" value={formatCount(data.totalMessages)} />
              <UserStatCard label="Total Feedback" value={formatCount(data.totalFeedback)} />
            </div>
          </div>

          <div className="space-y-3">
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-[var(--color-textSecondary)]">
              Activity Ratios
            </p>
            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
              <UserStatCard label="Chats / User" value={formatRatio(data.averageChatsPerUser)} />
              <UserStatCard label="Messages / Chat" value={formatRatio(data.averageMessagesPerChat)} />
              <UserStatCard label="Messages / User" value={formatRatio(data.averageMessagesPerUser)} />
            </div>
          </div>
        </div>
      )}
    </section>
  );
};

export default AdminStatisticsSection;
