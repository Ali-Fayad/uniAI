import type { AdminUserFeedbackResponse } from '../../../../types/dto';
import LoadingSpinner from '../../../common/LoadingSpinner';

interface SelectedUserFeedbackTabProps {
  feedback: AdminUserFeedbackResponse[];
  isLoading: boolean;
  error: string | null;
}

const formatDate = (value: string) => new Date(value).toLocaleString();

const SelectedUserFeedbackTab = ({ feedback, isLoading, error }: SelectedUserFeedbackTabProps) => {
  if (isLoading) {
    return (
      <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-10">
        <LoadingSpinner text="Loading feedback..." />
      </div>
    );
  }

  if (error) {
    return (
      <div
        role="alert"
        className="rounded-2xl border border-[var(--color-error)]/40 bg-[var(--color-error)]/10 px-4 py-4 text-sm text-[var(--color-textPrimary)]"
      >
        {error}
      </div>
    );
  }

  if (feedback.length === 0) {
    return (
      <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-8 text-sm text-[var(--color-textSecondary)]">
        No feedback found.
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {feedback.map((item) => (
        <article
          key={item.id}
          className="rounded-3xl border border-[var(--color-border)] bg-[var(--color-background)] px-5 py-4 shadow-sm"
        >
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-2">
              <span className="rounded-full border border-[var(--color-border)] bg-[var(--color-surface)] px-2.5 py-1 text-xs font-semibold text-[var(--color-textSecondary)]">
                Rating: {item.rating ?? '—'}
              </span>
              <span className="text-xs font-medium text-[var(--color-textSecondary)]">
                {formatDate(item.createdAt)}
              </span>
            </div>
          </div>
          <p className="mt-3 text-sm leading-6 text-[var(--color-textPrimary)]">
            {item.content}
          </p>
        </article>
      ))}
    </div>
  );
};

export default SelectedUserFeedbackTab;
