import { Navigate } from 'react-router-dom';
import { TokenStorage } from '../utils/storage';

interface PublicRouteProps {
  children: React.ReactNode;
}

export const PublicRoute = ({ children }: PublicRouteProps) => {
  const isAuthenticated = TokenStorage.isAuthenticated();

  if (isAuthenticated) {
    return <Navigate to="/chat" replace />;
  }

  return <>{children}</>;
};
