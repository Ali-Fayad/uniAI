import { Navigate } from 'react-router-dom';
import { TokenStorage } from '../utils/storage';

interface ProtectedRouteProps {
  children:  React.ReactNode;
}

export const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const isAuthenticated = TokenStorage.isAuthenticated();

  if (!isAuthenticated) {
    return <Navigate to="/auth/signin" replace />;
  }

  return <>{children}</>;
};
