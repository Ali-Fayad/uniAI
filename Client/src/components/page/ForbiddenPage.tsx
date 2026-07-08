import React from 'react';
import { Link } from 'react-router-dom';
import { PageTransition } from '../animations';
import { ROUTES } from '../../router';

const ForbiddenPage: React.FC = () => {
  return (
    <PageTransition>
      <main className="min-h-[calc(100vh-64px)] bg-[var(--color-background)] px-4 py-12 sm:px-6 lg:px-8">
        <div className="mx-auto flex w-full max-w-3xl items-center justify-center">
          <section className="w-full rounded-3xl border border-[var(--color-border)] bg-[var(--color-surface)] p-8 text-center shadow-xl sm:p-12">
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-[var(--color-primary)]">
              Forbidden
            </p>
            <h1 className="mt-4 text-3xl font-black tracking-tight text-[var(--color-textPrimary)] sm:text-4xl">
              You do not have permission to access this page.
            </h1>
            <p className="mt-4 text-base leading-7 text-[var(--color-textSecondary)]">
              If you expected access here, make sure you are signed in with an admin account.
            </p>
            <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
              <Link
                to={ROUTES.HOME}
                className="inline-flex items-center justify-center rounded-full border border-[var(--color-border)] px-6 py-3 font-medium text-[var(--color-textPrimary)] transition-colors hover:bg-[var(--color-elevatedSurface)]"
              >
                Back to home
              </Link>
              <Link
                to={ROUTES.CHAT}
                className="inline-flex items-center justify-center rounded-full bg-[var(--color-primary)] px-6 py-3 font-bold text-[var(--color-background)] transition-colors hover:bg-[var(--color-primaryVariant)]"
              >
                Go to chat
              </Link>
            </div>
          </section>
        </div>
      </main>
    </PageTransition>
  );
};

export default ForbiddenPage;
