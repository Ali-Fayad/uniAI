import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthCard } from "../../../components/AuthCard";
import { authService } from "../../../services/auth";
import { TEXT } from "../../../constants/static";
import { ROUTES } from "../../../router";

const ForgotPassword = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  const handleForgotPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess(false);
    setIsLoading(true);

    try {
      await authService.forgotPassword(email);
      setSuccess(true);

      // Navigate to confirmation page after a short delay
      setTimeout(() => {
        navigate(ROUTES.FORGOT_PASSWORD_CONFIRM, { state: { email } });
      }, 1500);
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        setError(axiosError.response?.data?.message || TEXT.auth.forgotPassword.error);
      } else {
        setError(TEXT.common.error);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] py-12 px-4">
      <AuthCard>
        <div className="flex flex-col gap-2 mb-8 pt-8">
          <p className="text-[var(--color-textPrimary)] text-4xl font-black tracking-[-0.033em]">{TEXT.auth.forgotPassword.title}</p>
          <p className="text-[var(--color-textSecondary)]">{TEXT.auth.forgotPassword.subtitle}</p>
        </div>

        <form className="space-y-6" onSubmit={handleForgotPassword}>
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {success && (
            <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded-lg text-sm">
              {TEXT.auth.forgotPassword.success}
            </div>
          )}

          <div className="flex flex-col">
            <label className="flex flex-col w-full">
              <p className="text-[var(--color-textPrimary)] font-medium pb-2">{TEXT.auth.forgotPassword.emailLabel}</p>
              <input
                type="email"
                placeholder={TEXT.auth.forgotPassword.emailPlaceholder}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)]"
              />
            </label>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="flex w-full items-center justify-center rounded-full h-12 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? TEXT.auth.forgotPassword.submitButtonLoading : TEXT.auth.forgotPassword.submitButton}
          </button>
        </form>

        <p className="text-center text-[var(--color-textSecondary)] text-sm mt-6">
          Remember your password?{" "}
          <button
            type="button"
            className="text-[var(--color-primaryVariant)] font-medium hover:text-[var(--color-primary)] transition-colors ml-1 bg-transparent p-0 border-0"
            onClick={() => navigate("/signin")}
          >
            Sign in
          </button>
        </p>
      </AuthCard>
    </div>
  );
};

export default ForgotPassword;
