import type { FormEvent } from 'react';
import LoadingSpinner from '../../../common/LoadingSpinner';
import UserRoleBadge from '../components/UserRoleBadge';
import { useAdminUserSearchController } from './useAdminUserSearchController';

interface AdminUserSearchSectionProps {
  id: string;
  labelledBy: string;
}

const AdminUserSearchSection = ({ id, labelledBy }: AdminUserSearchSectionProps) => {
  const controller = useAdminUserSearchController();

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    void controller.handleSearch();
  };

  return (
    <section
      id={id}
      role="tabpanel"
      aria-labelledby={labelledBy}
      className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-8 sm:px-6"
    >
      <div className="space-y-6">
        <div className="space-y-2">
          <p className="text-base font-semibold text-[var(--color-textPrimary)]">User Search</p>
          <p className="text-sm leading-6 text-[var(--color-textSecondary)]">
            Search users by email to inspect a selected account later.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3">
          <label className="block text-sm font-medium text-[var(--color-textPrimary)]" htmlFor="admin-user-search-email">
            Email
          </label>
          <div className="flex flex-col gap-3 sm:flex-row">
            <input
              id="admin-user-search-email"
              type="text"
              autoComplete="off"
              autoCapitalize="none"
              spellCheck={false}
              value={controller.emailQuery}
              onChange={(event) => controller.setEmailQuery(event.target.value)}
              placeholder="search@example.com"
              className="min-w-0 flex-1 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-3 text-sm text-[var(--color-textPrimary)] placeholder:text-[var(--color-textSecondary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
            />
            <button
              type="submit"
              disabled={controller.isLoading}
              className="inline-flex items-center justify-center rounded-xl bg-[var(--color-primary)] px-5 py-3 text-sm font-semibold text-[var(--color-background)] transition-colors disabled:cursor-not-allowed disabled:opacity-70"
            >
              {controller.isLoading ? 'Searching...' : 'Search'}
            </button>
          </div>
          {controller.helperMessage && !controller.error && (
            <p className="text-sm text-[var(--color-textSecondary)]" aria-live="polite">
              {controller.helperMessage}
            </p>
          )}
        </form>

        {controller.error && (
          <div
            role="alert"
            className="rounded-2xl border border-[var(--color-error)]/40 bg-[var(--color-error)]/10 px-4 py-3 text-sm text-[var(--color-textPrimary)]"
          >
            {controller.error}
          </div>
        )}

        {controller.isLoading && (
          <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-10">
            <LoadingSpinner text="Searching users..." />
          </div>
        )}

        {!controller.isLoading && !controller.error && controller.hasSearched && controller.results.length === 0 && (
          <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-8 text-sm text-[var(--color-textSecondary)]">
            No users found.
          </div>
        )}

        {!controller.isLoading && !controller.error && !controller.hasSearched && (
          <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-8 text-sm text-[var(--color-textSecondary)]">
            Enter an email above to search for a user.
          </div>
        )}

        {!controller.isLoading && controller.results.length > 0 && (
          <div className="space-y-3">
            <p className="text-sm font-medium text-[var(--color-textSecondary)]">
              {controller.results.length} result{controller.results.length === 1 ? '' : 's'} found
            </p>
            <div className="space-y-3">
              {controller.results.map((user) => {
                const isSelected = controller.selectedUser?.id === user.id;

                return (
                  <button
                    key={user.id}
                    type="button"
                    onClick={() => controller.handleSelectUser(user)}
                    className={[
                      'w-full rounded-2xl border px-4 py-4 text-left transition-colors',
                      isSelected
                        ? 'border-[var(--color-primary)] bg-[var(--color-primary)]/10'
                        : 'border-[var(--color-border)] bg-[var(--color-background)] hover:bg-[var(--color-elevatedSurface)]',
                    ].join(' ')}
                  >
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                      <div className="space-y-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <p className="text-base font-semibold text-[var(--color-textPrimary)]">{user.email}</p>
                          <UserRoleBadge role={user.role} />
                        </div>
                        <p className="text-sm text-[var(--color-textSecondary)]">
                          {user.firstName} {user.lastName} · @{user.username}
                        </p>
                      </div>
                      <span className="text-xs font-medium text-[var(--color-textSecondary)]">
                        {isSelected ? 'Selected' : 'Select'}
                      </span>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {controller.selectedUser && (
          <div className="rounded-2xl border border-[var(--color-primary)]/20 bg-[var(--color-primary)]/10 px-4 py-4">
            <p className="text-sm font-semibold text-[var(--color-textPrimary)]">
              Selected user details dialog will be implemented in ADMIN-DASHBOARD-014B.
            </p>
            <p className="mt-1 text-sm text-[var(--color-textSecondary)]">
              {controller.selectedUser.email} · {controller.selectedUser.firstName} {controller.selectedUser.lastName}
            </p>
          </div>
        )}
      </div>
    </section>
  );
};

export default AdminUserSearchSection;
