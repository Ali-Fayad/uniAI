import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../../hooks/useAuth";
import LoadingSpinner from "../../common/LoadingSpinner";

/**
 * GoogleCallback component to handle OAuth redirect
 * Expects token as query parameter
 */
const GoogleCallback = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { login } = useAuth();
  const [error, setError] = useState("");

  useEffect(() => {
    const handleCallback = () => {
      // Get token from query parameters
      const token = searchParams.get('token');
      const errorParam = searchParams.get('error');

      if (errorParam) {
        setError(errorParam);
        // Redirect to auth page after showing error
        setTimeout(() => {
          navigate('/auth');
        }, 3000);
        return;
      }

      if (token) {
        // Store token and redirect to chat
        login(token);
        navigate('/chat');
      } else {
        setError('No authentication token received');
        setTimeout(() => {
          navigate('/auth');
        }, 3000);
      }
    };

    handleCallback();
  }, [searchParams, login, navigate]);

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-custom-light">
        <div className="max-w-md w-full bg-white/50 backdrop-blur-sm p-8 rounded-3xl shadow-lg border border-white/20 text-center">
          <div className="mb-4">
            <span className="material-symbols-outlined text-red-500 text-5xl">error</span>
          </div>
          <h2 className="text-2xl font-bold text-[#151514] mb-2">Authentication Failed</h2>
          <p className="text-[#797672] mb-4">{error}</p>
          <p className="text-sm text-[#797672]">Redirecting to login page...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-custom-light">
      <div className="max-w-md w-full bg-white/50 backdrop-blur-sm p-8 rounded-3xl shadow-lg border border-white/20 text-center">
        <LoadingSpinner size="large" text="Completing Google Sign In..." />
      </div>
    </div>
  );
};

export default GoogleCallback;
