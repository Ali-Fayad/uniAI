import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { TokenStorage } from "../utils/storage";

type Props = {
  children: React.ReactElement;
};

/**
 * Protects routes that require authentication.
 * If the user is not authenticated, redirect to the sign-in page.
 */
const RequireAuth: React.FC<Props> = ({ children }) => {
  const location = useLocation();
  const authenticated = TokenStorage.isAuthenticated();

  if (!authenticated) {
    // Redirect to sign in, preserve where the user was trying to go
    return <Navigate to="/auth/signin" state={{ from: location }} replace />;
  }

  return children;
};

export default RequireAuth;
