import LoadingSpinner from '../../../common/LoadingSpinner';

interface AdminUserDeleteConfirmDialogProps {
  open: boolean;
  userLabel: string;
  isLoading: boolean;
  error: string | null;
  onCancel: () => void;
  onConfirm: () => void;
}

const AdminUserDeleteConfirmDialog = ({
  open,
  userLabel,
  isLoading,
  error,
  onCancel,
  onConfirm,
}: AdminUserDeleteConfirmDialogProps) => {
  if (!open) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/60 p-4">
      <div
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="admin-user-delete-title"
        className="w-full max-w-lg rounded-3xl border border-[var(--color-border)] bg-[var(--color-surface)] shadow-2xl"
      >
        <div className="border-b border-[var(--color-border)] px-5 py-4 sm:px-6">
          <h3 id="admin-user-delete-title" className="text-xl font-black tracking-tight text-[var(--color-textPrimary)]">
            Delete User
          </h3>
          <p className="mt-2 text-sm leading-6 text-[var(--color-textSecondary)]">
            This will delete {userLabel} and their associated data. This cannot be undone.
          </p>
        </div>

        <div className="space-y-4 px-5 py-5 sm:px-6">
          {error && (
            <div
              role="alert"
              className="rounded-2xl border border-[var(--color-error)]/40 bg-[var(--color-error)]/10 px-4 py-3 text-sm text-[var(--color-textPrimary)]"
            >
              {error}
            </div>
          )}

          {isLoading && (
            <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-8">
              <LoadingSpinner text="Deleting user..." />
            </div>
          )}

          <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
            <button
              type="button"
              onClick={onCancel}
              disabled={isLoading}
              className="inline-flex items-center justify-center rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-2.5 text-sm font-semibold text-[var(--color-textPrimary)] transition-colors hover:bg-[var(--color-elevatedSurface)] disabled:cursor-not-allowed disabled:opacity-70"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={onConfirm}
              disabled={isLoading}
              className="inline-flex items-center justify-center rounded-xl border border-[var(--color-error)]/40 bg-[var(--color-error)]/10 px-4 py-2.5 text-sm font-semibold text-[var(--color-error)] transition-colors hover:bg-[var(--color-error)]/15 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isLoading ? 'Deleting...' : 'Delete User'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminUserDeleteConfirmDialog;
