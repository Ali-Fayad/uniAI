import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import { useLocation, useNavigate } from "react-router-dom";
import { AuthCard } from "../../../components/AuthCard";
import { authService } from "../../../services/auth";
import { userService } from "../../../services/user";
import { useAuth } from "../../../hooks/useAuth";
import { TEXT } from "../../../constants/static";
import { ROUTES } from "../../../router";
import type { VerifyDto } from "../../../types/dto";
import AnimatedInput from "../../../components/common/AnimatedInput";

const Verify = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuth();
  const [code, setCode] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [isResending, setIsResending] = useState(false);
  const [resendMessage, setResendMessage] = useState("");
  const [resendError, setResendError] = useState("");
  const [cooldownSeconds, setCooldownSeconds] = useState(0);
  const redirectTo =
    (location.state as { redirectTo?: string } | null)?.redirectTo || ROUTES.CHAT;

  // Get email from navigation state
  const email = (location.state as { email?: string })?.email || "";

  useEffect(() => {
    if (cooldownSeconds <= 0) {
      return;
    }

    const timer = window.setTimeout(() => {
      setCooldownSeconds((current) => Math.max(current - 1, 0));
    }, 1000);

    return () => window.clearTimeout(timer);
  }, [cooldownSeconds]);

  const formatCooldown = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = String(seconds % 60).padStart(2, "0");
    return `${minutes}:${remainingSeconds}`;
  };

  const startCooldown = (seconds = 60) => {
    setCooldownSeconds((current) => (current > 0 ? current : seconds));
  };

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

      let personalInfoExists = false;
      try {
        personalInfoExists = await userService.hasPersonalInfo();
      } catch {
        personalInfoExists = false;
      }

      if (personalInfoExists) {
        navigate(redirectTo, { replace: true });
      } else {
        navigate(ROUTES.WELCOME, {
          replace: true,
          state: { redirectTo },
        });
      }
    } catch (err: unknown) {
      if (err && typeof err === "object" && "response" in err) {
        const axiosError = err as {
          response?: { data?: { message?: string } | string };
        };

        const backendMessage =
          typeof axiosError.response?.data === 'string'
            ? axiosError.response.data
            : axiosError.response?.data?.message;

        setError(
          backendMessage ||
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
    if (!email) {
      setResendError(TEXT.auth.verify.resendMissingEmail);
      setResendMessage("");
      return;
    }

    setIsResending(true);
    setResendError("");
    setResendMessage("");

    try {
      const response = await authService.resendVerificationCode(email);
      setResendMessage(response.message || TEXT.auth.verify.resendSuccess);
      setCooldownSeconds(60);
    } catch (err: unknown) {
      if (isAxiosError(err)) {
        const backendMessage =
          typeof err.response?.data === "string"
            ? err.response.data
            : err.response?.data?.message;

        if (err.response?.status === 429) {
          startCooldown(60);
        }

        setResendError(
          backendMessage ||
            (err.response?.status === 429
              ? TEXT.auth.verify.resendCooldown
              : TEXT.common.error)
        );
      } else {
        setResendError(TEXT.common.error);
      }
    } finally {
      setIsResending(false);
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
            <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
              {error}
            </div>
          )}

          {(resendMessage || resendError) && (
            <div
              className={[
                "rounded-lg border px-4 py-3 text-sm",
                resendError
                  ? "border-red-200 bg-red-50 text-red-800"
                  : "border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-textPrimary)]",
              ].join(" ")}
              aria-live="polite"
            >
              {resendError || resendMessage}
            </div>
          )}

          <div className="flex flex-col">
            <AnimatedInput
              type="text"
              label="Code"
              inputMode="numeric"
              pattern="[A-Za-z0-9]{6}"
              autoComplete="one-time-code"
              
              value={code}
              onChange={(e) =>
                setCode(e.target.value.replace(/[^A-Za-z0-9]/g, "").slice(0, 6))
              }
              required
              maxLength={6}
              className="tracking-widest text-lg"
            />
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
            disabled={isResending || cooldownSeconds > 0}
            className="ml-1 border-0 bg-transparent p-0 font-medium text-[var(--color-primaryVariant)] transition-colors hover:text-[var(--color-primary)] disabled:cursor-not-allowed disabled:opacity-60"
            onClick={handleResend}
          >
            {isResending
              ? TEXT.auth.verify.resendButtonLoading
              : cooldownSeconds > 0
                ? `Resend code in ${formatCooldown(cooldownSeconds)}`
                : TEXT.auth.verify.resendButton}
          </button>
        </p>
      </AuthCard>
    </div>
  );
};

export default Verify;
