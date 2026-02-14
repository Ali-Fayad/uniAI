import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { AuthCard } from "../../../components/AuthCard";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { authService } from "../../../services/auth";
import { useAuth } from "../../../hooks/useAuth";
import { TEXT } from "../../../constants/static";
import { ROUTES } from "../../../router";
import type { RequestPasswordDto } from "../../../types/dto";

const ForgotPasswordConfirm = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [code, setCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  // Get email from navigation state
  const email = (location.state as { email?: string })?.email || "";

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!email) {
      setError("Email is required. Please start the reset process again.");
      return;
    }

    if (newPassword !== confirmPassword) {
      setError(TEXT.auth.forgotPasswordConfirm.errors.passwordMismatch);
      return;
    }

    setError("");
    setIsLoading(true);

    try {
      const data: RequestPasswordDto = { email, verificationCode: code, newPassword };
      const response = await authService.forgotPasswordConfirm(data);

      // Store token (AuthContext will extract user from token)
      login(response.token);

      // Redirect to chat page
      navigate(ROUTES.CHAT);
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        setError(axiosError.response?.data?.message || TEXT.auth.forgotPasswordConfirm.errors.resetFailed);
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
            {TEXT.auth.forgotPasswordConfirm.title}
          </p>
          <p className="text-[var(--color-textSecondary)]">
            {TEXT.auth.forgotPasswordConfirm.subtitle}
          </p>
        </div>

        <form className="space-y-6" onSubmit={handleResetPassword}>
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <div className="flex flex-col">
            <label className="flex flex-col w-full">
              <p className="text-[var(--color-textPrimary)] font-medium pb-2">Reset Code</p>
              <input
                type="text"
                placeholder={TEXT.auth.verify.codePlaceholder}
                value={code}
                onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                required
                maxLength={6}
                className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)] tracking-widest"
              />
            </label>
          </div>

          <div className="relative">
            <label className="flex flex-col w-full">
              <p className="text-[var(--color-textPrimary)] font-medium pb-2">New Password</p>
              <input
                type={showPassword ? "text" : "password"}
                placeholder={TEXT.auth.forgotPasswordConfirm.newPasswordPlaceholder}
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
                minLength={8}
                className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)] pr-14"
              />
            </label>
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-4 top-[42px] text-[var(--color-primaryVariant)] hover:text-[var(--color-primary)] transition-colors bg-transparent p-0"
            >
              {showPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
            </button>
          </div>

          <div className="relative">
            <label className="flex flex-col w-full">
              <p className="text-[var(--color-textPrimary)] font-medium pb-2">Confirm Password</p>
              <input
                type={showConfirmPassword ? "text" : "password"}
                placeholder={TEXT.auth.forgotPasswordConfirm.confirmPasswordPlaceholder}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                minLength={8}
                className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)] pr-14"
              />
            </label>
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              className="absolute right-4 top-[42px] text-[var(--color-primaryVariant)] hover:text-[var(--color-primary)] transition-colors bg-transparent p-0"
            >
              {showConfirmPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
            </button>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="flex w-full items-center justify-center rounded-full h-12 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? TEXT.auth.forgotPasswordConfirm.submitButtonLoading : TEXT.auth.forgotPasswordConfirm.submitButton}
          </button>
        </form>

        <p className="text-center text-[var(--color-textSecondary)] text-sm mt-6">
          Remember your password?{" "}
          <button
            type="button"
            className="text-[var(--color-primaryVariant)] font-medium hover:text-[var(--color-primary)] transition-colors ml-1 bg-transparent p-0 border-0"
            onClick={() => navigate(ROUTES.SIGN_IN)}
          >
            Sign in
          </button>
        </p>
      </AuthCard>
    </div>
  );
};

export default ForgotPasswordConfirm;
