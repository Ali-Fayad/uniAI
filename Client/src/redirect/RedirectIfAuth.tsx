import React from "react";
import { Navigate } from "react-router-dom";
import { TokenStorage } from "../utils/storage";

type Props = {
  children: React.ReactElement;
};

/**
 * Redirects authenticated users away from auth pages (signin/signup/etc.)
 * to the main authenticated area (here: /chat).
 */
const RedirectIfAuth: React.FC<Props> = ({ children }) => {
  const authenticated = TokenStorage.isAuthenticated();

  if (authenticated) {
    return <Navigate to="/chat" replace />;
  }

  return children;
};

export default RedirectIfAuth;
