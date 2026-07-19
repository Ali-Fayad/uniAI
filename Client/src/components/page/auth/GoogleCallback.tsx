import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../../hooks/useAuth";
import LoadingSpinner from "../../common/LoadingSpinner";
import { authService } from "../../../services/auth";
import { STORAGE_KEYS } from "../../../constants";

/**
 * GoogleCallback component to handle OAuth redirect
 * Expects token as query parameter
 */
const GoogleCallback = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { login } = useAuth();
  const [error, setError] = useState("");
  const submitted = useRef(false);

  useEffect(() => {
    if (submitted.current) return;
    submitted.current = true;

    const handleCallback = async () => {
      const code = searchParams.get('code');
      const returnedState = searchParams.get('state');
      const errorParam = searchParams.get('error');
      const expectedState = sessionStorage.getItem(STORAGE_KEYS.GOOGLE_OAUTH_STATE);
      const redirectUri = `${window.location.origin}/google/callback`;

      try {
        if (errorParam) throw new Error('Google authentication was cancelled or rejected');
        if (!code) throw new Error('Google authorization code is missing');
        if (!returnedState || !expectedState || returnedState !== expectedState) {
          throw new Error('Google authentication state is invalid');
        }
        const response = await authService.completeGoogleAuth({ code, redirectUri });
        login(response.token);
        navigate('/chat', { replace: true });
      } catch (callbackError) {
        setError(callbackError instanceof Error ? callbackError.message : 'Unable to authenticate with Google');
        window.setTimeout(() => navigate('/auth', { replace: true }), 3000);
      } finally {
        sessionStorage.removeItem(STORAGE_KEYS.GOOGLE_OAUTH_STATE);
      }
    };

    void handleCallback();
  }, [searchParams, login, navigate]);

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--color-background)]">
        <div className="max-w-md w-full bg-[var(--color-surface)] p-8 rounded-3xl shadow-lg border border-[var(--color-border)] text-center">
          <div className="mb-4">
            <span className="material-symbols-outlined text-[var(--color-error)] text-5xl">error</span>
          </div>
          <h2 className="text-2xl font-bold text-[var(--color-textPrimary)] mb-2">Authentication Failed</h2>
          <p className="text-[var(--color-textSecondary)] mb-4">{error}</p>
          <p className="text-sm text-[var(--color-textSecondary)]">Redirecting to login page...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[var(--color-background)]">
      <div className="max-w-md w-full bg-[var(--color-surface)] p-8 rounded-3xl shadow-lg border border-[var(--color-border)] text-center">
        <LoadingSpinner size="large" text="Completing Google Sign In..." />
      </div>
    </div>
  );
};

export default GoogleCallback;
