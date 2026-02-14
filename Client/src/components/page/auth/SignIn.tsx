import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthCard } from "../../../components/AuthCard";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { authService } from "../../../services/auth";
import { useAuth } from "../../../hooks/useAuth";
import { TEXT } from "../../../constants/static";
import { ROUTES } from "../../../router";
import type { SignInDto } from "../../../types/dto";

const SignIn = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      const data: SignInDto = { email, password };
      const response = await authService.signIn(data);

      // Store token and user data
      login(response.token);

      // Redirect to chat page
      navigate(ROUTES.CHAT);
    } catch (err: unknown) {
      if (err && typeof err === "object" && "response" in err) {
        const axiosError = err as {
          response?: { status?: number; data?: { message?: string } };
        };

        // Handle 202 status (verification required)
        // 2FA flow: backend returns 401 when 2FA is required
        if (axiosError.response?.status === 401) {
          navigate(ROUTES.VERIFY_2FA, { state: { email } });
          return;
        }

        // Email verification flow: backend returns 202
        if (axiosError.response?.status === 202) {
          navigate(ROUTES.VERIFY, { state: { email } });
          return;
        }

        setError(
          axiosError.response?.data?.message ||
            TEXT.auth.signIn.errors.invalidCredentials
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
            {TEXT.auth.signIn.title}
          </p>
          <p className="text-[var(--color-textSecondary)] text-base">
            {TEXT.auth.signIn.subtitle}
          </p>
        </div>

        <form className="space-y-6" onSubmit={handleLogin}>
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {/* EMAIL FIELD */}
          <div className="flex flex-col">
            <label className="flex flex-col w-full pb-2">
              <p className="text-[var(--color-textPrimary)] font-medium pb-2">{TEXT.auth.signIn.emailLabel}</p>
              <input
                type="email"
                placeholder={TEXT.auth.signIn.emailPlaceholder}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)]"
              />
            </label>
          </div>

          {/* PASSWORD FIELD */}
          <div className="flex flex-col w-full">
            {/* Label + Forgot Password */}
            <div className="flex justify-between items-center pb-2">
              <p className="text-[var(--color-textPrimary)] font-medium">{TEXT.auth.signIn.passwordLabel}</p>

              <button
                type="button"
                onClick={() => navigate(ROUTES.FORGOT_PASSWORD)}
                className="text-[var(--color-primaryVariant)] text-sm font-medium hover:text-[var(--color-primary)] transition-colors bg-transparent p-0"
              >
                {TEXT.auth.signIn.forgotPassword}
              </button>
            </div>

            {/* Input + Toggle Icon */}
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                placeholder={TEXT.auth.signIn.passwordPlaceholder}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)] pr-14"
              />

              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-[var(--color-primaryVariant)] hover:text-[var(--color-primary)] transition-colors bg-transparent p-0"
              >
                {showPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
              </button>
            </div>
          </div>

          {/* SIGN IN BUTTON */}
          <div>
            <button
              type="submit"
              disabled={isLoading}
              className="flex w-full items-center justify-center rounded-full h-12 px-5 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? TEXT.auth.signIn.submitButtonLoading : TEXT.auth.signIn.submitButton}
            </button>
          </div>
        </form>

        <p className="text-center text-[var(--color-textSecondary)] text-sm mt-6">
          {TEXT.auth.signIn.noAccount}{" "}
          <button
            type="button"
            className="text-[var(--color-primaryVariant)] font-medium hover:text-[var(--color-primary)] transition-colors ml-1 bg-transparent p-0"
            onClick={() => navigate(ROUTES.SIGN_UP)}
          >
            {TEXT.auth.signIn.signUpLink}
          </button>
        </p>
      </AuthCard>
    </div>
  );
};

export default SignIn;
