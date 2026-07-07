import React from 'react';
import { PageTransition } from '../../animations';
import AdminDashboardPageShell from './AdminDashboardPageShell';
import { useAdminDashboardController } from './useAdminDashboardController';

const AdminDashboardPage: React.FC = () => {
  const controller = useAdminDashboardController();

  return (
    <PageTransition>
      <AdminDashboardPageShell controller={controller} />
    </PageTransition>
  );
};

export default AdminDashboardPage;
