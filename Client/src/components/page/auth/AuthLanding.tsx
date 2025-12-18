import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { authService } from '../../../services/auth';

/**
 * Auth Landing page - provides options to sign in, sign up, or use OAuth
 */
const AuthLanding = () => {
  const navigate = useNavigate();
  const [isLoadingGoogle, setIsLoadingGoogle] = useState(false);

  const handleGoogleAuth = async () => {
    setIsLoadingGoogle(true);
    try {
      const response = await authService.getGoogleAuthUrl();
      // Redirect to Google OAuth URL
      window.location.href = response.url;
    } catch (error) {
      console.error('Failed to get Google auth URL:', error);
      setIsLoadingGoogle(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] py-12 px-4">
      <div className="grid grid-cols-1 lg:grid-cols-3 w-full max-w-6xl min-h-[600px] rounded-3xl overflow-hidden shadow-2xl">
        {/* Left Panel - Information */}
        <div className="hidden lg:flex flex-col items-center justify-center bg-[var(--color-accent)] p-8 relative">
          <div className="z-10 text-center text-[var(--color-textPrimary)]">
            <svg
              className="mx-auto h-12 w-auto text-[var(--color-primary)] mb-6"
              fill="currentColor"
              viewBox="0 0 54 44"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path d="M26.5816 43.3134L53.1633 0H39.8724L26.5816 26.5816L13.2908 0H0L26.5816 43.3134Z"></path>
            </svg>
            <h2 className="text-3xl font-black tracking-tight text-[var(--color-textPrimary)] opacity-80 mb-4">
              Why You Must Log In?
            </h2>
            <p className="text-base text-[var(--color-textPrimary)] opacity-60">
              Logging in allows you to save your chats and data, so you can continue
              your conversations anytime, without losing progress.
            </p>
          </div>
        </div>

        {/* Right Panel - Auth Options */}
        <div className="col-span-1 lg:col-span-2 flex flex-col items-center justify-center p-6 sm:p-12 bg-[var(--color-surface)]">
          <div className="w-full max-w-md">
            <div className="flex flex-col gap-2 mb-8 pt-4 text-center">
              <p className="text-[var(--color-textPrimary)] text-4xl font-black tracking-[-0.033em]">
                Welcome Back
              </p>
              <p className="text-[var(--color-textSecondary)]">Sign in to continue to your dashboard.</p>
            </div>

            <div className="space-y-4">
              {/* Sign In Button */}
              <button
                type="button"
                onClick={() => navigate('/signin')}
                className="flex w-full items-center justify-center rounded-full h-12 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition"
              >
                Sign In
              </button>

              <div className="relative my-6">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-[var(--color-border)]"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="bg-[var(--color-surface)] px-2 text-[var(--color-textSecondary)]">or</span>
                </div>
              </div>

              {/* Sign Up Button */}
              <button
                type="button"
                onClick={() => navigate('/signup')}
                className="flex w-full items-center justify-center rounded-full h-12 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition"
              >
                Sign Up
              </button>

              <div className="relative my-6">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-[var(--color-border)]"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="bg-[var(--color-surface)] px-2 text-[var(--color-textSecondary)]">or continue with</span>
                </div>
              </div>

              {/* OAuth Buttons */}
              <div className="space-y-3">
                <button
                  onClick={handleGoogleAuth}
                  disabled={isLoadingGoogle}
                  className="flex w-full items-center justify-center gap-2 rounded-full border border-[var(--color-border)] bg-[var(--color-surface)] h-12 text-[var(--color-textPrimary)] font-medium hover:bg-[var(--color-elevatedSurface)] transition disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isLoadingGoogle ? 'Loading...' : 'Google'}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AuthLanding;
