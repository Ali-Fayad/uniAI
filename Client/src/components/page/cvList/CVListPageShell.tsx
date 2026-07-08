import LoadingSpinner from '../../common/LoadingSpinner';
import type { UseCVListControllerReturn } from './useCVListController';

interface CVListPageShellProps {
  controller: UseCVListControllerReturn;
}

const CVListPageShell = ({ controller }: CVListPageShellProps) => {
  if (controller.isLoading) {
    return (
      <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] px-4 py-10">
        <LoadingSpinner text="Loading CVs..." />
      </div>
    );
  }

  return (
    <main className="min-h-[calc(100vh-64px)] bg-[var(--color-background)] px-4 py-8 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-5xl space-y-6">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-3xl font-bold text-[var(--color-textPrimary)]">Your CVs</h1>
            <p className="mt-1 text-sm text-[var(--color-textSecondary)]">
              Manage CV configurations and continue editing anytime.
            </p>
          </div>

          <button
            type="button"
            onClick={controller.createNew}
            className="rounded-md bg-[var(--color-primary)] px-4 py-2 text-sm font-semibold text-[var(--color-background)]"
          >
            Create New CV
          </button>
        </div>

        {controller.error && (
          <div className="rounded-md border border-[var(--color-error)]/40 bg-[var(--color-error)]/10 px-4 py-3 text-sm text-[var(--color-textPrimary)]">
            {controller.error}
          </div>
        )}

        <div className="space-y-3">
          {controller.cvs.length === 0 ? (
            <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-8 text-center text-sm text-[var(--color-textSecondary)]">
              No CVs yet. Create your first CV configuration.
            </div>
          ) : (
            controller.cvs.map((cv) => (
              <div
                key={cv.id}
                className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4"
              >
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <div className="flex items-center gap-2">
                      <h2 className="text-base font-semibold text-[var(--color-textPrimary)]">{cv.cvName}</h2>
                      {cv.isDefault && (
                        <span className="rounded-full bg-[var(--color-primary)]/15 px-2 py-0.5 text-xs font-medium text-[var(--color-primary)]">
                          Default
                        </span>
                      )}
                    </div>
                    <p className="mt-1 text-sm text-[var(--color-textSecondary)]">
                      Template: {cv.templateName || cv.templateComponentName || 'N/A'}
                    </p>
                    <p className="mt-1 text-xs text-[var(--color-textSecondary)]">
                      Last modified: {new Date(cv.updatedAt).toLocaleString()}
                    </p>
                  </div>

                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={() => controller.editCv(cv.id)}
                      className="rounded-md border border-[var(--color-border)] px-3 py-1.5 text-sm text-[var(--color-textPrimary)]"
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => void controller.deleteCv(cv.id)}
                      className="rounded-md border border-[var(--color-error)]/40 px-3 py-1.5 text-sm text-[var(--color-error)]"
                    >
                      Delete
                    </button>
                    {!cv.isDefault && (
                      <button
                        type="button"
                        onClick={() => void controller.setDefaultCv(cv.id)}
                        className="rounded-md bg-[var(--color-primary)]/15 px-3 py-1.5 text-sm text-[var(--color-primary)]"
                      >
                        Set as Default
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </main>
  );
};

export default CVListPageShell;
