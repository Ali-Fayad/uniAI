import React from 'react';

interface ProfileIncompleteDialogProps {
  open: boolean;
  onConfirm: () => void;
}

const ProfileIncompleteDialog: React.FC<ProfileIncompleteDialogProps> = ({
  open,
  onConfirm,
}) => {
  if (!open) {
    return null;
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4"
        role="dialog"
        aria-modal="true"
        aria-labelledby="profile-incomplete-dialog-title"
      >
        <div className="w-full max-w-md rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 shadow-2xl">
        <div className="flex items-start gap-3">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-[var(--color-primary)]/15 text-[var(--color-primary)]">
            <span className="material-symbols-outlined">info</span>
          </div>

          <div className="min-w-0">
            <h2
              id="profile-incomplete-dialog-title"
              className="text-xl font-bold text-[var(--color-textPrimary)]"
            >
              Complete your profile
            </h2>
            <p className="mt-2 text-sm leading-6 text-[var(--color-textSecondary)]">
              Some personal information is missing. Completing it first will make
              building your CV much easier.
            </p>
          </div>
        </div>

        <div className="mt-6 flex justify-end">
          <button
            type="button"
            onClick={onConfirm}
            className="rounded-full bg-[var(--color-primary)] px-4 py-2 text-sm font-semibold text-[var(--color-background)] transition-colors hover:bg-[var(--color-primaryVariant)]"
          >
            OK
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProfileIncompleteDialog;
