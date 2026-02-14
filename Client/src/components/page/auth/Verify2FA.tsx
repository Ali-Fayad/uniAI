import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { AuthCard } from "../../../components/AuthCard";
import { authService } from "../../../services/auth";
import { useAuth } from "../../../hooks/useAuth";
import { TEXT } from "../../../constants/static";
import { ROUTES } from "../../../router";
import type { VerifyDto } from "../../../types/dto";

const Verify2FA = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [code, setCode] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  // Get email from navigation state
  const email = (location.state as { email?: string })?.email || "";

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!email) {
      setError("Email is required. Please sign in again.");
      return;
    }

    setError("");
    setIsLoading(true);

    try {
      const data: VerifyDto = { email, verificationCode: code };
      const response = await authService.verify2FA(data);

      // Store token and user data
      login(response.token);
      // Redirect to chat page
      navigate(ROUTES.CHAT);
    } catch (err: unknown) {
      if (err && typeof err === "object" && "response" in err) {
        const axiosError = err as {
          response?: { data?: { message?: string } };
        };
        setError(
          axiosError.response?.data?.message ||
            TEXT.auth.verify2FA.error
        );
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
          <p className="text-[var(--color-textPrimary)] text-4xl font-black tracking-[-0.033em]">
            {TEXT.auth.verify2FA.title}
          </p>
          <p className="text-[var(--color-textSecondary)]">
            {TEXT.auth.verify2FA.subtitle}
          </p>
        </div>

        <form className="space-y-6" onSubmit={handleVerify}>
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <div className="flex flex-col">
            <label className="flex flex-col w-full">
              <p className="text-[var(--color-textPrimary)] font-medium pb-2">Code</p>
              <input
                type="text"
                placeholder={TEXT.auth.verify2FA.codePlaceholder}
                value={code}
                onChange={(e) => setCode(e.target.value)}
                required
                maxLength={128}
                className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)] text-lg"
              />
            </label>
          </div>

          <button
            type="submit"
            disabled={isLoading || code.length === 0}
            className="flex w-full items-center justify-center rounded-full h-12 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? TEXT.auth.verify2FA.submitButtonLoading : TEXT.auth.verify2FA.submitButton}
          </button>
        </form>

        <p className="text-center text-[var(--color-textSecondary)] text-sm mt-6">
          <button
            type="button"
            className="text-[var(--color-primaryVariant)] font-medium hover:text-[var(--color-primary)] transition-colors bg-transparent p-0 border-0"
            onClick={() => navigate(ROUTES.SIGN_IN)}
          >
            Back to Sign In
          </button>
        </p>
      </AuthCard>
    </div>
  );
};

export default Verify2FA;
