import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import LoadingSpinner from './LoadingSpinner';
import { ROUTES } from '../../router';
import type { UserRole } from '../../types/dto';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: UserRole | UserRole[];
}

/**
 * ProtectedRoute
 *
 * Responsible for guarding routes that require authentication.
 *
 * Does NOT perform authentication itself and does NOT perform API calls.
 */
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRole }) => {
  const location = useLocation();
  const { isAuthenticated, isLoading, user } = useAuth();

  // Show loading spinner while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--color-background)]">
        <LoadingSpinner />
      </div>
    );
  }

  // Redirect to auth page if not authenticated
  if (!isAuthenticated) {
    const returnTo = encodeURIComponent(`${location.pathname}${location.search}`);
    return <Navigate to={`${ROUTES.SIGN_IN}?returnTo=${returnTo}`} replace />;
  }

  if (requiredRole) {
    const allowedRoles = Array.isArray(requiredRole) ? requiredRole : [requiredRole];
    const userRole = user?.role;
    if (!userRole || !allowedRoles.includes(userRole)) {
      return <Navigate to={ROUTES.FORBIDDEN} replace />;
    }
  }

  // Render children if authenticated
  return <>{children}</>;
};

export default ProtectedRoute;
