import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { AuthCard } from "../../../components/AuthCard";
import { authService } from "../../../services/auth";
import { useAuth } from "../../../hooks/useAuth";
import { TEXT } from "../../../constants/static";
import { ROUTES } from "../../../router";
import type { VerifyDto } from "../../../types/dto";

const Verify = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuth();
  const [code, setCode] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  // Get email from navigation state
  const email = (location.state as { email?: string })?.email || "";

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!email) {
      setError("Email is required. Please start the signup process again.");
      return;
    }

    // validate code as a 6-character alphanumeric string (server uses codes like ABCD12)
    const trimmedCode = code.trim();
    if (!/^([A-Za-z0-9]){6}$/.test(trimmedCode)) {
      setError("Please enter the 6-character verification code.");
      return;
    }

    setError("");
    setIsLoading(true);

    try {
      // backend expects field name `verificationCode`
      const payload = { email, verificationCode: trimmedCode } as VerifyDto;
      const response = await authService.verify(payload);

      // Store token (AuthContext will extract user from token if not provided)
      login(response.token);
      // Redirect to chat after successful verification
      navigate(ROUTES.CHAT);
    } catch (err: unknown) {
      if (err && typeof err === "object" && "response" in err) {
        const axiosError = err as {
          response?: { data?: { message?: string } };
        };
        setError(
          axiosError.response?.data?.message ||
            TEXT.auth.verify.error
        );
      } else {
        setError(TEXT.common.error);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleResend = async () => {
    if (!email) return;

    try {
      // Resend verification by calling signup again (backend should handle this)
      console.log("Resending verification code to:", email);
      // TODO: Implement resend endpoint if available
    } catch (err) {
      console.error("Failed to resend code:", err);
    }
  };

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] py-12 px-4">
      <AuthCard>
        <div className="flex flex-col gap-2 mb-8 pt-8">
          <p className="text-[var(--color-textPrimary)] text-4xl font-black tracking-[-0.033em]">
            {TEXT.auth.verify.title}
          </p>
          <p className="text-[var(--color-textSecondary)]">
            {TEXT.auth.verify.subtitle}
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
                inputMode="numeric"
                pattern="[A-Za-z0-9]{6}"
                autoComplete="one-time-code"
                placeholder={TEXT.auth.verify.codePlaceholder}
                value={code}
                onChange={(e) =>
                  setCode(e.target.value.replace(/[^A-Za-z0-9]/g, "").slice(0, 6))
                }
                required
                maxLength={6}
                className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)] tracking-widest text-lg"
              />
            </label>
          </div>

          <button
            type="submit"
            disabled={isLoading || code.length !== 6}
            className="flex w-full items-center justify-center rounded-full h-12 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? TEXT.auth.verify.submitButtonLoading : TEXT.auth.verify.submitButton}
          </button>
        </form>

        <p className="text-center text-[var(--color-textSecondary)] text-sm mt-6">
          Didn't receive code?{" "}
          <button
            type="button"
            className="text-[var(--color-primaryVariant)] font-medium hover:text-[var(--color-primary)] transition-colors ml-1 bg-transparent p-0 border-0"
            onClick={handleResend}
          >
            Resend
          </button>
        </p>
      </AuthCard>
    </div>
  );
};

export default Verify;
