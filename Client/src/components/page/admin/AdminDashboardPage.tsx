import React from 'react';
import { PageTransition } from '../../animations';

const AdminDashboardPage: React.FC = () => {
  return (
    <PageTransition>
      <main className="min-h-[calc(100vh-64px)] bg-[var(--color-background)] px-4 py-12 sm:px-6 lg:px-8">
        <div className="mx-auto flex w-full max-w-4xl flex-col items-center justify-center">
          <section className="w-full rounded-3xl border border-[var(--color-border)] bg-[var(--color-surface)] p-8 shadow-xl sm:p-12">
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-[var(--color-primary)]">
              Admin Dashboard
            </p>
            <h1 className="mt-4 text-4xl font-black tracking-tight text-[var(--color-textPrimary)] sm:text-5xl">
              Admin access granted
            </h1>
            <p className="mt-4 max-w-2xl text-base leading-7 text-[var(--color-textSecondary)] sm:text-lg">
              This route is reserved for admin users. Dashboard analytics and management tools will be added here later.
            </p>
          </section>
        </div>
      </main>
    </PageTransition>
  );
};

export default AdminDashboardPage;
