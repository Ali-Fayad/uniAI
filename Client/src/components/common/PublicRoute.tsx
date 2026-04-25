import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import LoadingSpinner from './LoadingSpinner';
import { ROUTES } from '../../router';

interface PublicRouteProps {
  children: React.ReactNode;
}

/**
 * PublicRoute
 *
 * Prevents authenticated users from accessing auth-only entry pages.
 * Authenticated users are sent to the main protected entry page.
 *
 * Keep this redirect stable and side-effect free. Sending users through
 * additional onboarding redirects from here can create blank transitions
 * when auth-only pages bounce immediately after login.
 */
const PublicRoute: React.FC<PublicRouteProps> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--color-background)]">
        <LoadingSpinner />
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to={ROUTES.CHAT} replace />;
  }

  return <>{children}</>;
};

export default PublicRoute;
